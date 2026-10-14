package com.carpooling.trip.service;

import com.carpooling.common.event.ReservationEvent;
import com.carpooling.common.event.TripEvent;
import com.carpooling.trip.config.RabbitMQConfig;
import com.carpooling.trip.entity.OutboxEvent;
import com.carpooling.trip.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final AmqpTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    private static final int MAX_RETRIES = 5;

    @Scheduled(fixedDelayString = "${outbox.publisher.delay:3000}")
    @Transactional
    public void processOutboxEvents() {
        List<OutboxEvent> pendingEvents = outboxRepository.findTopPendingEvents("PENDING", PageRequest.of(0, 50));
        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("A processar {} eventos pendentes na Outbox...", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                Object eventPayload;
                if ("TRIP".equalsIgnoreCase(event.getAggregateType())) {
                    eventPayload = objectMapper.readValue(event.getPayload(), TripEvent.class);
                } else if ("RESERVATION".equalsIgnoreCase(event.getAggregateType())) {
                    eventPayload = objectMapper.readValue(event.getPayload(), ReservationEvent.class);
                } else {
                    eventPayload = objectMapper.readValue(event.getPayload(), Object.class);
                }

                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, event.getRoutingKey(), eventPayload);

                event.setStatus("SENT");
                event.setProcessedAt(LocalDateTime.now());
                event.setErrorMessage(null);
                log.info("Evento Outbox #{} [{}] publicado com sucesso para RabbitMQ (chave: {})",
                        event.getId(), event.getEventType(), event.getRoutingKey());
            } catch (Exception e) {
                event.setRetryCount(event.getRetryCount() + 1);
                event.setErrorMessage(e.getMessage());
                if (event.getRetryCount() >= MAX_RETRIES) {
                    event.setStatus("FAILED");
                    log.error("Evento Outbox #{} atingiu o limite de {} tentativas e foi marcado como FAILED: {}",
                            event.getId(), MAX_RETRIES, e.getMessage());
                } else {
                    log.warn("Erro ao publicar evento Outbox #{} (tentativa {}/{}): {}",
                            event.getId(), event.getRetryCount(), MAX_RETRIES, e.getMessage());
                }
            }
            outboxRepository.save(event);
        }
    }
}

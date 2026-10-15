package com.carpooling.trip.service;

import com.carpooling.common.event.ReservationEvent;
import com.carpooling.common.event.TripEvent;
import com.carpooling.trip.config.RabbitMQConfig;
import com.carpooling.trip.entity.OutboxEvent;
import com.carpooling.trip.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    private static final int MAX_RETRIES = 5;
    private static final long CONFIRM_TIMEOUT_SECONDS = 5;

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

                CorrelationData correlationData = new CorrelationData(String.valueOf(event.getId()));
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, event.getRoutingKey(), eventPayload, correlationData);

                try {
                    CorrelationData.Confirm confirm = correlationData.getFuture().get(CONFIRM_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    if (confirm != null && !confirm.isAck()) {
                        throw new IllegalStateException("RabbitMQ NACK recebido para evento Outbox #" + event.getId()
                                + (confirm.getReason() != null ? ": " + confirm.getReason() : ""));
                    }
                } catch (TimeoutException te) {
                    log.warn("Confirmação de publicação não obtida dentro de {}s para evento Outbox #{}; mantendo PENDING para reprocessamento",
                            CONFIRM_TIMEOUT_SECONDS, event.getId());
                    throw te;
                }

                event.setStatus("SENT");
                event.setProcessedAt(LocalDateTime.now());
                event.setErrorMessage(null);
                log.info("Evento Outbox #{} [{}] publicado e confirmado com sucesso no RabbitMQ (chave: {})",
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

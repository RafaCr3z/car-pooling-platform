package com.carpooling.trip.service;

import com.carpooling.trip.entity.OutboxEvent;
import com.carpooling.trip.repository.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    /**
     * Guarda um evento na tabela outbox na mesma transação local do método de negócio.
     */
    @Transactional
    public void saveEvent(String aggregateType, Long aggregateId, String eventType, String routingKey, Object payload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .routingKey(routingKey)
                    .payload(jsonPayload)
                    .status("PENDING")
                    .retryCount(0)
                    .createdAt(LocalDateTime.now())
                    .build();

            outboxRepository.save(outboxEvent);
            log.info("Evento gravado na Outbox: agregado={} id={} tipo={}", aggregateType, aggregateId, eventType);
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar payload para Outbox: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao gravar evento na Outbox", e);
        }
    }
}

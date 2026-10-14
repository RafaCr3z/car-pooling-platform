package com.carpooling.trip;

import com.carpooling.common.event.TripEvent;
import com.carpooling.trip.config.RabbitMQConfig;
import com.carpooling.trip.entity.OutboxEvent;
import com.carpooling.trip.repository.OutboxRepository;
import com.carpooling.trip.service.OutboxPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - OutboxPublisher (Transactional Outbox Pattern)")
class OutboxPublisherTest {

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private AmqpTemplate rabbitTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @InjectMocks
    private OutboxPublisher outboxPublisher;

    private OutboxEvent sampleEvent;

    @BeforeEach
    void setUp() {
        sampleEvent = OutboxEvent.builder()
                .id(1L)
                .aggregateType("TRIP")
                .aggregateId(100L)
                .eventType("STARTED")
                .routingKey("trip.started")
                .payload("{\"eventType\":\"STARTED\",\"tripId\":100,\"driverId\":10,\"departureLocation\":\"Castelo Branco\",\"destinationLocation\":\"Lisboa\"}")
                .status("PENDING")
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Deve publicar evento pendente da Outbox no RabbitMQ e marcar como SENT")
    void testProcessOutboxEventsSuccess() {
        when(outboxRepository.findTopPendingEvents(eq("PENDING"), any(Pageable.class)))
                .thenReturn(List.of(sampleEvent));

        outboxPublisher.processOutboxEvents();

        assertEquals("SENT", sampleEvent.getStatus());
        assertNotNull(sampleEvent.getProcessedAt());
        verify(rabbitTemplate, times(1)).convertAndSend(eq(RabbitMQConfig.EXCHANGE_NAME), eq("trip.started"), any(TripEvent.class));
        verify(outboxRepository, times(1)).save(sampleEvent);
    }

    @Test
    @DisplayName("Deve incrementar retry_count e manter PENDING quando o broker RabbitMQ falha")
    void testProcessOutboxEventsRetryOnFailure() {
        when(outboxRepository.findTopPendingEvents(eq("PENDING"), any(Pageable.class)))
                .thenReturn(List.of(sampleEvent));
        doThrow(new RuntimeException("RabbitMQ connection refused"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        outboxPublisher.processOutboxEvents();

        assertEquals("PENDING", sampleEvent.getStatus());
        assertEquals(1, sampleEvent.getRetryCount());
        assertEquals("RabbitMQ connection refused", sampleEvent.getErrorMessage());
        verify(outboxRepository, times(1)).save(sampleEvent);
    }

    @Test
    @DisplayName("Deve marcar evento como FAILED quando atinge o limite de 5 tentativas")
    void testProcessOutboxEventsMaxRetriesExceeded() {
        sampleEvent.setRetryCount(4); // 5ª tentativa vai estourar o limite
        when(outboxRepository.findTopPendingEvents(eq("PENDING"), any(Pageable.class)))
                .thenReturn(List.of(sampleEvent));
        doThrow(new RuntimeException("Broker unreachable"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        outboxPublisher.processOutboxEvents();

        assertEquals("FAILED", sampleEvent.getStatus());
        assertEquals(5, sampleEvent.getRetryCount());
        verify(outboxRepository, times(1)).save(sampleEvent);
    }
}

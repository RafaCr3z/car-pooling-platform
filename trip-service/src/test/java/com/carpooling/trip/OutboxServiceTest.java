package com.carpooling.trip;

import com.carpooling.common.event.TripEvent;
import com.carpooling.trip.entity.OutboxEvent;
import com.carpooling.trip.repository.OutboxRepository;
import com.carpooling.trip.service.OutboxService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - OutboxService (Persistência Transacional de Eventos)")
class OutboxServiceTest {

    @Mock
    private OutboxRepository outboxRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @InjectMocks
    private OutboxService outboxService;

    @Test
    @DisplayName("Deve serializar payload e persistir evento na Outbox com estado PENDING")
    void testSaveEventSuccess() {
        TripEvent event = TripEvent.builder()
                .tripId(50L)
                .eventType(TripEvent.EventType.STARTED)
                .driverId(10L)
                .departureLocation("Castelo Branco")
                .destinationLocation("Lisboa")
                .timestamp(LocalDateTime.now())
                .build();

        outboxService.saveEvent("TRIP", 50L, "STARTED", "trip.started", event);

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxRepository, times(1)).save(captor.capture());

        OutboxEvent saved = captor.getValue();
        assertEquals("TRIP", saved.getAggregateType());
        assertEquals(50L, saved.getAggregateId());
        assertEquals("STARTED", saved.getEventType());
        assertEquals("trip.started", saved.getRoutingKey());
        assertEquals("PENDING", saved.getStatus());
        assertEquals(0, saved.getRetryCount());
        assertNotNull(saved.getPayload());
    }
}

package com.carpooling.common;

import com.carpooling.common.event.PaymentEvent;
import com.carpooling.common.event.ReservationEvent;
import com.carpooling.common.event.TripEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - Event Models (Contratos AMQP Comuns)")
class EventModelTest {

    @Test
    @DisplayName("Deve instanciar e serializar TripEvent com todos os campos")
    void testTripEventModel() {
        TripEvent event = TripEvent.builder()
                .tripId(1L)
                .driverId(10L)
                .eventType(TripEvent.EventType.CREATED)
                .departureLocation("Castelo Branco")
                .destinationLocation("Lisboa")
                .distanceKm(225.0)
                .co2SavedKg(25.8)
                .totalCost(new BigDecimal("30.00"))
                .costPerPassenger(new BigDecimal("10.00"))
                .availableSeats(3)
                .occupiedSeats(0)
                .timestamp(LocalDateTime.now())
                .build();

        assertNotNull(event);
        assertEquals(1L, event.getTripId());
        assertEquals("Castelo Branco", event.getDepartureLocation());
        assertEquals(TripEvent.EventType.CREATED, event.getEventType());
    }

    @Test
    @DisplayName("Deve instanciar ReservationEvent com metadados de condutor e passageiro")
    void testReservationEventModel() {
        ReservationEvent event = ReservationEvent.builder()
                .reservationId(50L)
                .tripId(1L)
                .passengerId(20L)
                .passengerName("Maria Santos")
                .driverId(10L)
                .eventType(ReservationEvent.EventType.CONFIRMED)
                .numberOfSeats(1)
                .totalPrice(new BigDecimal("10.00"))
                .build();

        assertNotNull(event);
        assertEquals(50L, event.getReservationId());
        assertEquals(ReservationEvent.EventType.CONFIRMED, event.getEventType());
        assertEquals(new BigDecimal("10.00"), event.getTotalPrice());
    }

    @Test
    @DisplayName("Deve instanciar PaymentEvent com método e estado de liquidação")
    void testPaymentEventModel() {
        PaymentEvent event = PaymentEvent.builder()
                .paymentId(100L)
                .tripId(1L)
                .reservationId(50L)
                .payerId(20L)
                .payerName("Maria Santos")
                .receiverId(10L)
                .amount(new BigDecimal("10.00"))
                .status(PaymentEvent.PaymentStatus.COMPLETED)
                .paymentMethod("MBWAY")
                .transactionReference("MBWAY-TEST1234")
                .timestamp(LocalDateTime.now())
                .build();

        assertNotNull(event);
        assertEquals(100L, event.getPaymentId());
        assertEquals("MBWAY", event.getPaymentMethod());
        assertEquals(PaymentEvent.PaymentStatus.COMPLETED, event.getStatus());
    }
}


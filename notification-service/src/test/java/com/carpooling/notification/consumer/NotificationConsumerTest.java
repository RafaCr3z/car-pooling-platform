package com.carpooling.notification.consumer;

import com.carpooling.common.event.ReservationEvent;
import com.carpooling.common.event.TripEvent;
import com.carpooling.notification.entity.Notification;
import com.carpooling.notification.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationConsumer notificationConsumer;

    @Test
    @DisplayName("Deve persistir notificação de viagem iniciada para o condutor")
    void testHandleTripEvent_Started() {
        TripEvent event = TripEvent.builder()
                .eventType(TripEvent.EventType.STARTED)
                .tripId(101L)
                .driverId(1L)
                .driverEmail("driver@carpooling.com")
                .departureLocation("Castelo Branco")
                .destinationLocation("Lisboa")
                .build();

        notificationConsumer.handleTripEvent(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertEquals(1L, saved.getUserId());
        assertEquals("driver@carpooling.com", saved.getUserEmail());
        assertEquals("TRIP_STARTED", saved.getType());
        assertEquals(101L, saved.getRelatedTripId());
        assertTrue(saved.getMessage().contains("Castelo Branco"));
        assertFalse(saved.getIsRead());
    }

    @Test
    @DisplayName("Deve persistir notificação de viagem concluída com cálculo de CO2")
    void testHandleTripEvent_Completed() {
        TripEvent event = TripEvent.builder()
                .eventType(TripEvent.EventType.COMPLETED)
                .tripId(102L)
                .driverId(2L)
                .driverEmail("driver2@carpooling.com")
                .departureLocation("Porto")
                .destinationLocation("Coimbra")
                .co2SavedKg(18.5)
                .build();

        notificationConsumer.handleTripEvent(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertEquals(2L, saved.getUserId());
        assertEquals("TRIP_COMPLETED", saved.getType());
        assertTrue(saved.getMessage().contains("kg de CO2"));
        assertTrue(saved.getMessage().contains("18"));
    }

    @Test
    @DisplayName("Deve notificar condutor quando surge novo pedido de reserva")
    void testHandleReservationEvent_Requested() {
        ReservationEvent event = ReservationEvent.builder()
                .eventType(ReservationEvent.EventType.REQUESTED)
                .reservationId(50L)
                .tripId(101L)
                .driverId(1L)
                .driverEmail("driver@carpooling.com")
                .passengerName("João Silva")
                .numberOfSeats(2)
                .departureLocation("Castelo Branco")
                .destinationLocation("Lisboa")
                .build();

        notificationConsumer.handleReservationEvent(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertEquals(1L, saved.getUserId());
        assertEquals("RESERVATION_REQUESTED", saved.getType());
        assertTrue(saved.getMessage().contains("João Silva"));
        assertTrue(saved.getMessage().contains("2 lugar(es)"));
    }

    @Test
    @DisplayName("Deve notificar passageiro quando a sua reserva é confirmada pelo condutor")
    void testHandleReservationEvent_Confirmed() {
        ReservationEvent event = ReservationEvent.builder()
                .eventType(ReservationEvent.EventType.CONFIRMED)
                .reservationId(51L)
                .tripId(101L)
                .passengerId(20L)
                .passengerEmail("passenger@carpooling.com")
                .driverName("Manuel Santos")
                .departureLocation("Castelo Branco")
                .destinationLocation("Lisboa")
                .build();

        notificationConsumer.handleReservationEvent(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertEquals(20L, saved.getUserId());
        assertEquals("RESERVATION_CONFIRMED", saved.getType());
        assertTrue(saved.getMessage().contains("Manuel Santos"));
    }

    @Test
    @DisplayName("Deve notificar passageiro quando o pedido de reserva é rejeitado")
    void testHandleReservationEvent_Rejected() {
        ReservationEvent event = ReservationEvent.builder()
                .eventType(ReservationEvent.EventType.REJECTED)
                .reservationId(52L)
                .tripId(101L)
                .passengerId(20L)
                .passengerEmail("passenger@carpooling.com")
                .departureLocation("Castelo Branco")
                .destinationLocation("Lisboa")
                .build();

        notificationConsumer.handleReservationEvent(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertEquals(20L, saved.getUserId());
        assertEquals("RESERVATION_REJECTED", saved.getType());
        assertTrue(saved.getMessage().contains("não pôde ser aceite"));
    }
}

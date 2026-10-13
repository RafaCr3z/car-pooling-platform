package com.carpooling.notification.consumer;

import com.carpooling.common.event.ReservationEvent;
import com.carpooling.common.event.TripEvent;
import com.carpooling.notification.entity.Notification;
import com.carpooling.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;

    @RabbitListener(queues = "notification.trip.queue")
    public void handleTripEvent(TripEvent event) {
        log.info("Notificação recebida para TripEvent: tipo={} viagemId={}", event.getEventType(), event.getTripId());

        if (event.getEventType() == TripEvent.EventType.STARTED) {
            Notification notif = Notification.builder()
                    .userId(event.getDriverId())
                    .userEmail(event.getDriverEmail())
                    .title("Viagem Iniciada!")
                    .message(String.format("A sua viagem de %s para %s foi iniciada. Boa viagem!", 
                            event.getDepartureLocation(), event.getDestinationLocation()))
                    .type("TRIP_STARTED")
                    .relatedTripId(event.getTripId())
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notif);
        } else if (event.getEventType() == TripEvent.EventType.COMPLETED) {
            Notification notif = Notification.builder()
                    .userId(event.getDriverId())
                    .userEmail(event.getDriverEmail())
                    .title("Viagem Concluída!")
                    .message(String.format("A viagem de %s para %s foi concluída. Poupou cerca de %.1f kg de CO2!", 
                            event.getDepartureLocation(), event.getDestinationLocation(), 
                            event.getCo2SavedKg() != null ? event.getCo2SavedKg() : 0.0))
                    .type("TRIP_COMPLETED")
                    .relatedTripId(event.getTripId())
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notif);
        }
    }

    @RabbitListener(queues = "notification.reservation.queue")
    public void handleReservationEvent(ReservationEvent event) {
        log.info("Notificação recebida para ReservationEvent: tipo={} reservaId={}", event.getEventType(), event.getReservationId());

        if (event.getEventType() == ReservationEvent.EventType.REQUESTED) {
            // Notificar o condutor de que recebeu um novo pedido de boleia
            Notification notif = Notification.builder()
                    .userId(event.getDriverId())
                    .userEmail(event.getDriverEmail())
                    .title("Novo Pedido de Reserva!")
                    .message(String.format("%s solicitou %d lugar(es) na viagem de %s para %s.",
                            event.getPassengerName(), event.getNumberOfSeats(),
                            event.getDepartureLocation(), event.getDestinationLocation()))
                    .type("RESERVATION_REQUESTED")
                    .relatedTripId(event.getTripId())
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notif);
        } else if (event.getEventType() == ReservationEvent.EventType.CONFIRMED) {
            // Notificar o passageiro de que o condutor confirmou a partilha
            Notification notif = Notification.builder()
                    .userId(event.getPassengerId())
                    .userEmail(event.getPassengerEmail())
                    .title("Reserva Confirmada!")
                    .message(String.format("O condutor %s confirmou a sua reserva na viagem de %s para %s.",
                            event.getDriverName(), event.getDepartureLocation(), event.getDestinationLocation()))
                    .type("RESERVATION_CONFIRMED")
                    .relatedTripId(event.getTripId())
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notif);
        } else if (event.getEventType() == ReservationEvent.EventType.REJECTED) {
            // Notificar o passageiro de que o pedido foi recusado
            Notification notif = Notification.builder()
                    .userId(event.getPassengerId())
                    .userEmail(event.getPassengerEmail())
                    .title("Pedido de Reserva Recusado")
                    .message(String.format("Lamentamos, mas o pedido de reserva na viagem de %s para %s não pôde ser aceite.",
                            event.getDepartureLocation(), event.getDestinationLocation()))
                    .type("RESERVATION_REJECTED")
                    .relatedTripId(event.getTripId())
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notif);
        }
    }
}

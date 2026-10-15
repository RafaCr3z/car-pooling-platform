package com.carpooling.trip;

import com.carpooling.trip.entity.OutboxEvent;
import com.carpooling.trip.entity.Reservation;
import com.carpooling.trip.entity.ReservationStatus;
import com.carpooling.trip.entity.Trip;
import com.carpooling.trip.entity.TripStatus;
import com.carpooling.trip.repository.OutboxRepository;
import com.carpooling.trip.repository.ReservationRepository;
import com.carpooling.trip.repository.TripRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:trip_integration_db;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDER=HIGH",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@DisplayName("Testes de Integração - Fluxo Reserva -> Transactional Outbox -> Persistência Real")
class ReservationOutboxFlowIntegrationTest {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Integração: Fluxo completo de persistência de Viagem, Reserva e Registo Transactional Outbox")
    void testReservationAndOutboxPersistenceFlow() {
        // 1. Criar e persistir viagem na base de dados
        Trip trip = Trip.builder()
                .driverId(101L)
                .driverName("Carlos Oliveira")
                .departureLocation("Coimbra")
                .destinationLocation("Porto")
                .distance(120.0)
                .availableSeats(3)
                .occupiedSeats(0)
                .totalCost(new BigDecimal("18.00"))
                .costPerPassenger(new BigDecimal("6.00"))
                .status(TripStatus.OPEN)
                .departureDateTime(LocalDateTime.now().plusDays(1))
                .co2SavedKg(0.0)
                .build();

        Trip savedTrip = tripRepository.save(trip);
        assertNotNull(savedTrip.getId(), "A viagem deve ser persistida com ID gerado");

        // 2. Criar e persistir reserva associada à viagem
        Reservation reservation = Reservation.builder()
                .trip(savedTrip)
                .passengerId(202L)
                .passengerName("Ana Pereira")
                .passengerEmail("ana.pereira@carpooling.pt")
                .numberOfSeats(2)
                .totalPrice(new BigDecimal("12.00"))
                .status(ReservationStatus.PENDING)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);
        assertNotNull(savedReservation.getId(), "A reserva deve ser persistida com ID gerado");

        // 3. Simular a escrita transacional de evento no Transactional Outbox
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .aggregateType("RESERVATION")
                .aggregateId(savedReservation.getId())
                .eventType("REQUESTED")
                .routingKey("reservation.requested")
                .payload("{\"reservationId\":" + savedReservation.getId() + ",\"passengerId\":202,\"tripId\":" + savedTrip.getId() + "}")
                .status("PENDING")
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        OutboxEvent savedOutbox = outboxRepository.save(outboxEvent);
        assertNotNull(savedOutbox.getId());

        entityManager.flush();
        entityManager.clear();

        // 4. Validação da consulta pessimista da Outbox (SKIP LOCKED)
        List<OutboxEvent> pending = outboxRepository.findTopPendingEvents("PENDING", PageRequest.of(0, 10));
        assertFalse(pending.isEmpty(), "Deve encontrar o evento pendente na Outbox");
        assertEquals("RESERVATION", pending.get(0).getAggregateType());
        assertEquals(savedReservation.getId(), pending.get(0).getAggregateId());
        assertEquals("PENDING", pending.get(0).getStatus());
    }

    @Test
    @DisplayName("Integração: Confirmação de Reserva, Atualização de Lotação e Transição de Estado da Outbox para SENT")
    void testConfirmReservationAndOutboxStatusTransition() {
        // 1. Criar viagem aberta
        Trip trip = Trip.builder()
                .driverId(105L)
                .driverName("Manuel Antunes")
                .departureLocation("Lisboa")
                .destinationLocation("Faro")
                .distance(280.0)
                .availableSeats(2)
                .occupiedSeats(0)
                .totalCost(new BigDecimal("30.00"))
                .costPerPassenger(new BigDecimal("15.00"))
                .status(TripStatus.OPEN)
                .departureDateTime(LocalDateTime.now().plusDays(2))
                .co2SavedKg(0.0)
                .build();
        Trip savedTrip = tripRepository.save(trip);

        // 2. Reserva solicitada
        Reservation reservation = Reservation.builder()
                .trip(savedTrip)
                .passengerId(303L)
                .passengerName("Beatriz Lima")
                .passengerEmail("beatriz.lima@carpooling.pt")
                .numberOfSeats(2)
                .totalPrice(new BigDecimal("30.00"))
                .status(ReservationStatus.PENDING)
                .build();
        Reservation savedRes = reservationRepository.save(reservation);

        // 3. Confirmar reserva: atualizar estado e lotação da viagem
        savedRes.setStatus(ReservationStatus.CONFIRMED);
        savedTrip.setOccupiedSeats(savedTrip.getOccupiedSeats() + savedRes.getNumberOfSeats());
        savedTrip.setStatus(TripStatus.FULL); // Lotação esgotada
        tripRepository.save(savedTrip);
        reservationRepository.save(savedRes);

        // 4. Registo Outbox confirmado
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .aggregateType("RESERVATION")
                .aggregateId(savedRes.getId())
                .eventType("CONFIRMED")
                .routingKey("reservation.confirmed")
                .payload("{\"reservationId\":" + savedRes.getId() + ",\"status\":\"CONFIRMED\"}")
                .status("PENDING")
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .build();
        outboxRepository.save(outboxEvent);

        entityManager.flush();
        entityManager.clear();

        // 5. Simular processamento da Outbox pelo publisher e marcação como SENT
        List<OutboxEvent> pending = outboxRepository.findTopPendingEvents("PENDING", PageRequest.of(0, 10));
        assertEquals(1, pending.size());
        OutboxEvent toProcess = pending.get(0);
        toProcess.setStatus("SENT");
        toProcess.setProcessedAt(LocalDateTime.now());
        outboxRepository.save(toProcess);

        entityManager.flush();
        entityManager.clear();

        // 6. Verificar que a fila de pendentes ficou vazia
        List<OutboxEvent> remainingPending = outboxRepository.findTopPendingEvents("PENDING", PageRequest.of(0, 10));
        assertTrue(remainingPending.isEmpty(), "Não devem restar eventos PENDING na Outbox após envio");

        // 7. Verificar integridade da viagem na base de dados
        Trip reloadedTrip = tripRepository.findById(savedTrip.getId()).orElseThrow();
        assertEquals(TripStatus.FULL, reloadedTrip.getStatus());
        assertEquals(2, reloadedTrip.getOccupiedSeats());
    }

    @Test
    @DisplayName("Integração: Cancelamento de Reserva liberta os lugares da Viagem na Base de Dados")
    void testCancelReservationReleasesSeats() {
        Trip trip = Trip.builder()
                .driverId(108L)
                .driverName("Diana Ramos")
                .departureLocation("Braga")
                .destinationLocation("Guimarães")
                .distance(25.0)
                .availableSeats(4)
                .occupiedSeats(2)
                .totalCost(new BigDecimal("10.00"))
                .costPerPassenger(new BigDecimal("2.50"))
                .status(TripStatus.OPEN)
                .departureDateTime(LocalDateTime.now().plusDays(3))
                .co2SavedKg(0.0)
                .build();
        Trip savedTrip = tripRepository.save(trip);

        Reservation reservation = Reservation.builder()
                .trip(savedTrip)
                .passengerId(404L)
                .passengerName("Rui Costa")
                .numberOfSeats(2)
                .totalPrice(new BigDecimal("5.00"))
                .status(ReservationStatus.CONFIRMED)
                .build();
        Reservation savedRes = reservationRepository.save(reservation);

        // Cancelar reserva e repor lugares
        savedRes.setStatus(ReservationStatus.CANCELLED);
        savedTrip.setOccupiedSeats(savedTrip.getOccupiedSeats() - savedRes.getNumberOfSeats());
        tripRepository.save(savedTrip);
        reservationRepository.save(savedRes);

        entityManager.flush();
        entityManager.clear();

        Trip updatedTrip = tripRepository.findById(savedTrip.getId()).orElseThrow();
        assertEquals(0, updatedTrip.getOccupiedSeats(), "Os 2 lugares devem ser devolvidos à viagem");
        assertEquals(TripStatus.OPEN, updatedTrip.getStatus());
    }
}

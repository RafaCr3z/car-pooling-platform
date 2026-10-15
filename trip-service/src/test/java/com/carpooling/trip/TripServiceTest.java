package com.carpooling.trip;

import com.carpooling.common.security.CurrentUser;
import com.carpooling.trip.client.LocationClient;
import com.carpooling.trip.client.VehicleClient;
import com.carpooling.trip.dto.CreateReservationRequest;
import com.carpooling.trip.dto.CreateTripRequest;
import com.carpooling.trip.dto.ReservationDTO;
import com.carpooling.trip.dto.TripDTO;
import com.carpooling.trip.entity.Reservation;
import com.carpooling.trip.entity.ReservationStatus;
import com.carpooling.trip.entity.Trip;
import com.carpooling.trip.entity.TripStatus;
import com.carpooling.trip.repository.ReservationRepository;
import com.carpooling.trip.repository.TripRepository;
import com.carpooling.trip.service.OutboxService;
import com.carpooling.trip.service.TripService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - TripService (Criação de Viagens, Divisão de Custos e Reservas)")
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private VehicleClient vehicleClient;

    @Mock
    private LocationClient locationClient;

    @Mock
    private AmqpTemplate rabbitTemplate;

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private TripService tripService;

    private Trip sampleTrip;

    @BeforeEach
    void setUp() {
        sampleTrip = Trip.builder()
                .id(1L)
                .driverId(10L)
                .driverName("João Silva")
                .departureLocation("Castelo Branco")
                .destinationLocation("Lisboa")
                .distance(225.0)
                .availableSeats(3)
                .occupiedSeats(0)
                .totalCost(new BigDecimal("30.00"))
                .costPerPassenger(new BigDecimal("10.00"))
                .status(TripStatus.OPEN)
                .departureDateTime(LocalDateTime.now().plusDays(2))
                .build();
    }

    @Test
    @DisplayName("Deve criar viagem e calcular a divisão justa de despesas por passageiro")
    void testCreateTripDivisionOfExpenses() {
        CreateTripRequest req = CreateTripRequest.builder()
                .driverId(10L)
                .driverName("João Silva")
                .departureLocation("Castelo Branco")
                .destinationLocation("Lisboa")
                .distance(225.0)
                .availableSeats(3)
                .totalCost(new BigDecimal("30.00"))
                .build();

        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> {
            Trip t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        TripDTO created = tripService.createTrip(req);

        assertNotNull(created);
        assertEquals(3, created.getAvailableSeats());
        assertEquals(new BigDecimal("30.00"), created.getTotalCost());
        // 30.00 / 3 = 10.00 por passageiro
        assertEquals(new BigDecimal("10.00"), created.getCostPerPassenger());
        assertEquals("OPEN", created.getStatus());
        verify(tripRepository, times(1)).save(any(Trip.class));
    }

    @Test
    @DisplayName("Deve criar pedido de reserva com estado PENDING e decrementar lugares disponíveis")
    void testCreateReservationPending() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(sampleTrip));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation r = invocation.getArgument(0);
            r.setId(100L);
            return r;
        });

        CreateReservationRequest req = CreateReservationRequest.builder()
                .tripId(1L)
                .passengerId(20L)
                .passengerName("Maria Santos")
                .passengerEmail("maria@carpooling.pt")
                .numberOfSeats(1)
                .build();

        ReservationDTO res = tripService.createReservation(req);

        assertNotNull(res);
        assertEquals(100L, res.getId());
        assertEquals("PENDING", res.getStatus());
        assertEquals(new BigDecimal("10.00"), res.getTotalPrice());
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Deve rejeitar pedido de reserva quando não há lugares suficientes")
    void testCreateReservationInsufficientSeats() {
        sampleTrip.setOccupiedSeats(3); // viagem cheia
        when(tripRepository.findById(1L)).thenReturn(Optional.of(sampleTrip));

        CreateReservationRequest req = CreateReservationRequest.builder()
                .tripId(1L)
                .passengerId(20L)
                .numberOfSeats(1)
                .build();

        Exception ex = assertThrows(RuntimeException.class, () -> tripService.createReservation(req));
        assertTrue(ex.getMessage().contains("Lugares insuficientes"));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve confirmar reserva, incrementar lugares ocupados e calcular CO2 poupado")
    void testConfirmReservationCalculatesCo2() {
        Reservation reservation = Reservation.builder()
                .id(100L)
                .trip(sampleTrip)
                .passengerId(20L)
                .passengerName("Maria Santos")
                .numberOfSeats(1)
                .status(ReservationStatus.PENDING)
                .totalPrice(new BigDecimal("10.00"))
                .build();

        when(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
        when(tripRepository.save(any(Trip.class))).thenReturn(sampleTrip);

        ReservationDTO confirmed = tripService.confirmReservation(100L);

        assertNotNull(confirmed);
        assertEquals("CONFIRMED", confirmed.getStatus());
        assertEquals(1, sampleTrip.getOccupiedSeats());
        // CO2 = 225km * 0.115 * 1 ocupante = ~25.9 kg
        assertTrue(sampleTrip.getCo2SavedKg() > 20.0, "CO2 poupado deve ser superior a 20kg");
        verify(tripRepository, times(1)).save(sampleTrip);
        verify(reservationRepository, times(1)).save(reservation);
    }

    @Test
    @DisplayName("Deve rejeitar reserva e atualizar estado para REJECTED")
    void testRejectReservation() {
        Reservation reservation = Reservation.builder()
                .id(101L)
                .trip(sampleTrip)
                .passengerId(20L)
                .status(ReservationStatus.PENDING)
                .build();

        when(reservationRepository.findById(101L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        ReservationDTO result = tripService.rejectReservation(101L, "Sem disponibilidade");

        assertNotNull(result);
        assertEquals("REJECTED", result.getStatus());
        verify(reservationRepository).save(reservation);
        verify(outboxService).saveEvent(eq("RESERVATION"), eq(101L), eq("REJECTED"), anyString(), any());
    }

    @Test
    @DisplayName("Deve cancelar reserva confirmada e libertar os lugares na viagem")
    void testCancelReservationFreesSeats() {
        sampleTrip.setOccupiedSeats(2);
        Reservation reservation = Reservation.builder()
                .id(102L)
                .trip(sampleTrip)
                .passengerId(20L)
                .numberOfSeats(2)
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(reservationRepository.findById(102L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));
        when(tripRepository.save(any(Trip.class))).thenAnswer(i -> i.getArgument(0));

        ReservationDTO result = tripService.cancelReservation(102L);

        assertNotNull(result);
        assertEquals("CANCELLED", result.getStatus());
        assertEquals(0, sampleTrip.getOccupiedSeats());
        verify(tripRepository).save(sampleTrip);
        verify(reservationRepository).save(reservation);
    }

    @Test
    @DisplayName("Deve concluir viagem com estado COMPLETED e calcular emissões de CO2 poupadas")
    void testUpdateTripStatus_Completed() {
        sampleTrip.setOccupiedSeats(2);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(sampleTrip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(i -> i.getArgument(0));

        TripDTO completed = tripService.updateTripStatus(1L, "COMPLETED");

        assertNotNull(completed);
        assertEquals("COMPLETED", completed.getStatus());
        // 225km * 0.115 * 2 = 51.75 -> 51.8 kg
        assertTrue(completed.getCo2SavedKg() > 50.0);
        verify(tripRepository).save(sampleTrip);
        verify(outboxService).saveEvent(eq("TRIP"), eq(1L), eq("COMPLETED"), anyString(), any());
    }

    @Test
    @DisplayName("Deve cancelar viagem e publicar evento na Outbox")
    void testUpdateTripStatus_Cancelled() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(sampleTrip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(i -> i.getArgument(0));

        TripDTO cancelled = tripService.updateTripStatus(1L, "CANCELLED");

        assertNotNull(cancelled);
        assertEquals("CANCELLED", cancelled.getStatus());
        verify(outboxService).saveEvent(eq("TRIP"), eq(1L), eq("CANCELLED"), anyString(), any());
    }

    @Test
    @DisplayName("Deve listar viagens disponíveis com lugares vagos")
    void testGetAvailableTrips() {
        when(tripRepository.findAvailableTrips()).thenReturn(List.of(sampleTrip));

        List<TripDTO> trips = tripService.getAvailableTrips();

        assertEquals(1, trips.size());
        assertEquals("Castelo Branco", trips.get(0).getDepartureLocation());
    }

    @Test
    @DisplayName("Segurança IDOR: Deve impedir utilizador não-autorizado de confirmar reserva de outro condutor")
    void testConfirmReservation_ForbiddenForUnauthorizedUser() {
        Reservation reservation = Reservation.builder()
                .id(50L)
                .trip(sampleTrip) // driverId is 10L
                .passengerId(99L)
                .numberOfSeats(1)
                .status(ReservationStatus.PENDING)
                .build();

        when(reservationRepository.findById(50L)).thenReturn(Optional.of(reservation));

        CurrentUser intruder = new CurrentUser(999L, "intruder@evil.com", List.of("ROLE_PASSENGER"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                tripService.confirmReservation(50L, intruder)
        );

        assertEquals(403, ex.getStatusCode().value());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Segurança IDOR: Condutor legítimo da viagem pode confirmar reserva")
    void testConfirmReservation_AllowedForLegitimateDriver() {
        Reservation reservation = Reservation.builder()
                .id(50L)
                .trip(sampleTrip) // driverId is 10L
                .passengerId(99L)
                .numberOfSeats(1)
                .status(ReservationStatus.PENDING)
                .build();

        when(reservationRepository.findById(50L)).thenReturn(Optional.of(reservation));
        when(tripRepository.save(any(Trip.class))).thenAnswer(i -> i.getArgument(0));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        CurrentUser legitimateDriver = new CurrentUser(10L, "joao.silva@carpooling.pt", List.of("ROLE_DRIVER"));

        ReservationDTO confirmed = tripService.confirmReservation(50L, legitimateDriver);

        assertNotNull(confirmed);
        assertEquals("CONFIRMED", confirmed.getStatus());
        verify(reservationRepository).save(reservation);
    }

    @Test
    @DisplayName("Segurança IDOR: Deve impedir terceiro não-autorizado de cancelar reserva alheia")
    void testCancelReservation_ForbiddenForThirdParty() {
        Reservation reservation = Reservation.builder()
                .id(50L)
                .trip(sampleTrip) // driverId is 10L
                .passengerId(99L)
                .numberOfSeats(1)
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(reservationRepository.findById(50L)).thenReturn(Optional.of(reservation));

        CurrentUser stranger = new CurrentUser(777L, "stranger@other.com", List.of("ROLE_PASSENGER"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                tripService.cancelReservation(50L, stranger)
        );

        assertEquals(403, ex.getStatusCode().value());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Segurança IDOR: Passageiro titular pode cancelar a sua própria reserva")
    void testCancelReservation_AllowedForOwnerPassenger() {
        Reservation reservation = Reservation.builder()
                .id(50L)
                .trip(sampleTrip) // driverId is 10L
                .passengerId(99L)
                .numberOfSeats(1)
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(reservationRepository.findById(50L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        CurrentUser passenger = new CurrentUser(99L, "maria.santos@carpooling.pt", List.of("ROLE_PASSENGER"));

        ReservationDTO cancelled = tripService.cancelReservation(50L, passenger);

        assertNotNull(cancelled);
        assertEquals("CANCELLED", cancelled.getStatus());
        verify(reservationRepository).save(reservation);
    }
}



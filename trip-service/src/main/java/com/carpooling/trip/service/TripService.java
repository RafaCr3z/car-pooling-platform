package com.carpooling.trip.service;

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
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class TripService {

    private final TripRepository tripRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public TripDTO createTrip(CreateTripRequest request) {
        BigDecimal costPerPassenger = request.getTotalCost()
                .divide(BigDecimal.valueOf(request.getAvailableSeats()), 2, RoundingMode.HALF_UP);

        Trip trip = Trip.builder()
                .driverId(request.getDriverId())
                .departureLocation(request.getDepartureLocation())
                .destinationLocation(request.getDestinationLocation())
                .departureDateTime(request.getDepartureDateTime())
                .availableSeats(request.getAvailableSeats())
                .totalCost(request.getTotalCost())
                .costPerPassenger(costPerPassenger)
                .distance(request.getDistance())
                .occupiedSeats(0)
                .status(TripStatus.OPEN)
                .build();

        trip = tripRepository.save(trip);
        log.info("Trip created: {}", trip.getId());
        return mapToDTO(trip);
    }

    @Transactional(readOnly = true)
    @CircuitBreaker(name = "tripService", fallbackMethod = "getAllTripsFromDB")
    public List<TripDTO> getAllTrips() {
        return tripRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TripDTO> getAllTripsFromDB(Throwable t) {
        log.warn("Circuit breaker fallback triggered for getAllTrips: {}", t != null ? t.getMessage() : "unknown");
        return tripRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TripDTO getTripById(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Viagem não encontrada"));
        return mapToDTO(trip);
    }

    @Transactional(readOnly = true)
    public List<TripDTO> getTripsByDriver(Long driverId) {
        return tripRepository.findByDriverId(driverId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TripDTO> searchTrips(String departure, String destination, LocalDateTime departureTime) {
        return tripRepository.findTripsBetweenLocations(departure, destination, departureTime)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TripDTO> getAvailableTrips() {
        return tripRepository.findAvailableTrips()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TripDTO updateTripStatus(Long tripId, String status) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Viagem não encontrada"));

        try {
            trip.setStatus(TripStatus.valueOf(status.toUpperCase()));
            trip = tripRepository.save(trip);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Status inválido: " + status);
        }

        return mapToDTO(trip);
    }

    @Transactional
    public ReservationDTO createReservation(CreateReservationRequest request) {
        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new RuntimeException("Viagem não encontrada"));

        if (trip.getStatus() != TripStatus.OPEN) {
            throw new RuntimeException("Viagem não está disponível");
        }

        int totalOccupied = trip.getOccupiedSeats() + request.getNumberOfSeats();
        if (totalOccupied > trip.getAvailableSeats()) {
            throw new RuntimeException("Lugares insuficientes");
        }

        BigDecimal totalPrice = trip.getCostPerPassenger()
                .multiply(BigDecimal.valueOf(request.getNumberOfSeats()));

        Reservation reservation = Reservation.builder()
                .trip(trip)
                .passengerId(request.getPassengerId())
                .numberOfSeats(request.getNumberOfSeats())
                .totalPrice(totalPrice)
                .status(ReservationStatus.CONFIRMED)
                .build();

        reservation = reservationRepository.save(reservation);

        // Atualizar lugares ocupados na viagem
        trip.setOccupiedSeats(totalOccupied);
        if (totalOccupied == trip.getAvailableSeats()) {
            trip.setStatus(TripStatus.IN_PROGRESS);
        }
        tripRepository.save(trip);

        log.info("Reservation created: {}", reservation.getId());
        return mapReservationToDTO(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationDTO> getReservationsByPassenger(Long passengerId) {
        return reservationRepository.findByPassengerId(passengerId)
                .stream()
                .map(this::mapReservationToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReservationDTO> getReservationsByTrip(Long tripId) {
        return reservationRepository.findByTripId(tripId)
                .stream()
                .map(this::mapReservationToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReservationDTO updateReservationStatus(Long reservationId, String status) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));

        try {
            reservation.setStatus(ReservationStatus.valueOf(status.toUpperCase()));
            reservation = reservationRepository.save(reservation);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Status inválido: " + status);
        }

        return mapReservationToDTO(reservation);
    }

    private TripDTO mapToDTO(Trip trip) {
        return new TripDTO(
                trip.getId(),
                trip.getDriverId(),
                trip.getDepartureLocation(),
                trip.getDestinationLocation(),
                trip.getDepartureDateTime(),
                trip.getAvailableSeats(),
                trip.getOccupiedSeats(),
                trip.getTotalCost(),
                trip.getCostPerPassenger(),
                trip.getDistance(),
                trip.getStatus().name()
        );
    }

    private ReservationDTO mapReservationToDTO(Reservation reservation) {
        return new ReservationDTO(
                reservation.getId(),
                reservation.getTrip().getId(),
                reservation.getPassengerId(),
                reservation.getNumberOfSeats(),
                reservation.getTotalPrice(),
                reservation.getStatus().name()
        );
    }

}

package com.carpooling.trip.service;

import com.carpooling.common.dto.VehicleSummaryDTO;
import com.carpooling.common.event.ReservationEvent;
import com.carpooling.common.event.TripEvent;
import com.carpooling.trip.client.LocationClient;
import com.carpooling.trip.client.VehicleClient;
import com.carpooling.trip.config.RabbitMQConfig;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripService {

    private final TripRepository tripRepository;
    private final ReservationRepository reservationRepository;
    private final VehicleClient vehicleClient;
    private final LocationClient locationClient;
    private final AmqpTemplate rabbitTemplate;

    @Transactional
    public TripDTO createTrip(CreateTripRequest request) {
        log.info("A criar viagem para driverId={}: {} -> {}", 
                request.getDriverId(), request.getDepartureLocation(), request.getDestinationLocation());

        // 1. Obter detalhes do veículo via OpenFeign se vehicleId fornecido
        String vehicleInfo = "Carro Particular";
        Double co2EmissionsGPerKm = 115.0;
        if (request.getVehicleId() != null) {
            try {
                VehicleSummaryDTO vehicle = vehicleClient.getVehicleById(request.getVehicleId());
                if (vehicle != null) {
                    vehicleInfo = String.format("%s %s (%s)", vehicle.getBrand(), vehicle.getModel(), vehicle.getLicensePlate());
                    if (vehicle.getCo2EmissionsGPerKm() != null) {
                        co2EmissionsGPerKm = vehicle.getCo2EmissionsGPerKm();
                    }
                }
            } catch (Exception e) {
                log.warn("Erro ao obter veículo via Feign: {}. A usar valores por defeito.", e.getMessage());
            }
        }

        // 2. Obter rota e distâncias via OpenFeign se distância não fornecida
        Double distance = request.getDistance();
        Double originLat = request.getOriginLatitude();
        Double originLon = request.getOriginLongitude();
        Double destLat = request.getDestinationLatitude();
        Double destLon = request.getDestinationLongitude();
        BigDecimal estimatedFuelCost = request.getEstimatedFuelCost() != null ? request.getEstimatedFuelCost() : BigDecimal.ZERO;
        BigDecimal estimatedTolls = request.getEstimatedTolls() != null ? request.getEstimatedTolls() : BigDecimal.ZERO;

        if (distance == null || distance <= 0) {
            try {
                Map<String, Object> route = locationClient.getRouteBetweenCities(
                        request.getDepartureLocation(), 
                        request.getDestinationLocation()
                );
                if (route != null && route.containsKey("distanceKm")) {
                    distance = ((Number) route.get("distanceKm")).doubleValue();
                    originLat = ((Number) route.get("originLat")).doubleValue();
                    originLon = ((Number) route.get("originLon")).doubleValue();
                    destLat = ((Number) route.get("destinationLat")).doubleValue();
                    destLon = ((Number) route.get("destinationLon")).doubleValue();
                    if (route.containsKey("estimatedFuelCost")) {
                        estimatedFuelCost = new BigDecimal(route.get("estimatedFuelCost").toString());
                    }
                    if (route.containsKey("estimatedTolls")) {
                        estimatedTolls = new BigDecimal(route.get("estimatedTolls").toString());
                    }
                }
            } catch (Exception e) {
                log.warn("Erro ao obter rota via Feign: {}. A usar distância por defeito.", e.getMessage());
                distance = 150.0;
            }
        }

        if (distance == null) distance = 150.0;

        // 3. Cálculo do Custo Total e Custo por Passageiro
        BigDecimal totalCost = request.getTotalCost();
        if (totalCost == null || totalCost.compareTo(BigDecimal.ZERO) <= 0) {
            totalCost = estimatedFuelCost.add(estimatedTolls);
            if (totalCost.compareTo(BigDecimal.ZERO) <= 0) {
                totalCost = BigDecimal.valueOf(distance * 0.12).setScale(2, RoundingMode.HALF_UP);
            }
        }

        int availableSeats = request.getAvailableSeats() != null && request.getAvailableSeats() > 0 
                ? request.getAvailableSeats() : 3;

        BigDecimal costPerPassenger = totalCost.divide(
                BigDecimal.valueOf(availableSeats), 2, RoundingMode.HALF_UP
        );

        LocalDateTime departureTime = request.getDepartureDateTime() != null 
                ? request.getDepartureDateTime() 
                : LocalDateTime.now().plusDays(1).withHour(8).withMinute(0);

        Trip trip = Trip.builder()
                .driverId(request.getDriverId())
                .driverName(request.getDriverName() != null ? request.getDriverName() : "Condutor")
                .vehicleId(request.getVehicleId())
                .vehicleInfo(vehicleInfo)
                .departureLocation(request.getDepartureLocation())
                .destinationLocation(request.getDestinationLocation())
                .originLatitude(originLat)
                .originLongitude(originLon)
                .destinationLatitude(destLat)
                .destinationLongitude(destLon)
                .departureDateTime(departureTime)
                .availableSeats(availableSeats)
                .occupiedSeats(0)
                .totalCost(totalCost)
                .costPerPassenger(costPerPassenger)
                .estimatedFuelCost(estimatedFuelCost)
                .estimatedTolls(estimatedTolls)
                .distance(distance)
                .co2SavedKg(0.0)
                .description(request.getDescription())
                .status(TripStatus.OPEN)
                .build();

        trip = tripRepository.save(trip);
        log.info("Viagem criada com sucesso. ID: {}", trip.getId());

        // 4. Publicar evento no RabbitMQ
        publishTripEvent(trip, TripEvent.EventType.CREATED);

        return mapToDTO(trip);
    }

    @Transactional(readOnly = true)
    @CircuitBreaker(name = "tripService", fallbackMethod = "getAllTripsFallback")
    public List<TripDTO> getAllTrips() {
        return tripRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<TripDTO> getAllTripsFallback(Throwable t) {
        log.warn("Fallback do Circuit Breaker acionado para getAllTrips: {}", t != null ? t.getMessage() : "desconhecido");
        return Collections.emptyList();
    }

    @Transactional(readOnly = true)
    public TripDTO getTripById(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Viagem não encontrada com id: " + id));
        return mapToDTO(trip);
    }

    @Transactional(readOnly = true)
    public List<TripDTO> getTripsByDriver(Long driverId) {
        return tripRepository.findByDriverIdOrderByDepartureDateTimeDesc(driverId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TripDTO> searchTrips(String departure, String destination, LocalDateTime departureTime) {
        LocalDateTime time = departureTime != null ? departureTime : LocalDateTime.now().minusHours(2);
        String dep = (departure != null && !departure.isBlank()) ? departure.trim() : null;
        String dest = (destination != null && !destination.isBlank()) ? destination.trim() : null;

        return tripRepository.searchAvailableTrips(dep, dest, time)
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

        TripStatus newStatus = TripStatus.valueOf(status.toUpperCase());
        trip.setStatus(newStatus);

        if (newStatus == TripStatus.COMPLETED) {
            // Calcular CO2 total poupado: distância * emissão * passageiros ocupados
            double co2Factor = 0.115; // kg CO2 por km de um carro médio poupado por passageiro
            double saved = trip.getDistance() * co2Factor * trip.getOccupiedSeats();
            trip.setCo2SavedKg(BigDecimal.valueOf(saved).setScale(1, RoundingMode.HALF_UP).doubleValue());
            publishTripEvent(trip, TripEvent.EventType.COMPLETED);
        } else if (newStatus == TripStatus.CANCELLED) {
            publishTripEvent(trip, TripEvent.EventType.CANCELLED);
        }

        trip = tripRepository.save(trip);
        return mapToDTO(trip);
    }

    // ==========================================
    // RESERVAS
    // ==========================================

    @Transactional
    public ReservationDTO createReservation(CreateReservationRequest request) {
        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new RuntimeException("Viagem não encontrada"));

        if (trip.getStatus() != TripStatus.OPEN) {
            throw new RuntimeException("Esta viagem não se encontra aberta a novas reservas");
        }

        int requestedSeats = request.getNumberOfSeats() != null ? request.getNumberOfSeats() : 1;
        int remainingSeats = trip.getAvailableSeats() - trip.getOccupiedSeats();

        if (requestedSeats > remainingSeats) {
            throw new RuntimeException("Lugares insuficientes. Apenas restam " + remainingSeats + " lugares disponíveis.");
        }

        BigDecimal totalPrice = trip.getCostPerPassenger().multiply(BigDecimal.valueOf(requestedSeats));

        Reservation reservation = Reservation.builder()
                .trip(trip)
                .passengerId(request.getPassengerId())
                .passengerName(request.getPassengerName() != null ? request.getPassengerName() : "Passageiro")
                .passengerEmail(request.getPassengerEmail() != null ? request.getPassengerEmail() : "passageiro@carpooling.pt")
                .passengerPhone(request.getPassengerPhone())
                .numberOfSeats(requestedSeats)
                .totalPrice(totalPrice)
                .status(ReservationStatus.PENDING)
                .build();

        reservation = reservationRepository.save(reservation);
        log.info("Pedido de reserva criado. ID: {} para viagem: {}", reservation.getId(), trip.getId());

        // Publicar evento de reserva solicitada
        publishReservationEvent(reservation, ReservationEvent.EventType.REQUESTED);

        return mapReservationToDTO(reservation);
    }

    @Transactional
    public ReservationDTO confirmReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));

        Trip trip = reservation.getTrip();
        int remaining = trip.getAvailableSeats() - trip.getOccupiedSeats();

        if (reservation.getNumberOfSeats() > remaining) {
            throw new RuntimeException("Não há lugares suficientes para confirmar esta reserva");
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);
        trip.setOccupiedSeats(trip.getOccupiedSeats() + reservation.getNumberOfSeats());

        if (trip.getOccupiedSeats() >= trip.getAvailableSeats()) {
            trip.setStatus(TripStatus.FULL);
        }

        // Recalcular CO2 poupado projetado
        double saved = trip.getDistance() * 0.115 * trip.getOccupiedSeats();
        trip.setCo2SavedKg(BigDecimal.valueOf(saved).setScale(1, RoundingMode.HALF_UP).doubleValue());

        tripRepository.save(trip);
        reservation = reservationRepository.save(reservation);

        log.info("Reserva ID: {} confirmada pelo condutor para viagem: {}", reservationId, trip.getId());
        publishReservationEvent(reservation, ReservationEvent.EventType.CONFIRMED);

        return mapReservationToDTO(reservation);
    }

    @Transactional
    public ReservationDTO rejectReservation(Long reservationId, String reason) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));

        reservation.setStatus(ReservationStatus.REJECTED);
        reservation = reservationRepository.save(reservation);

        log.info("Reserva ID: {} rejeitada pelo condutor", reservationId);
        publishReservationEvent(reservation, ReservationEvent.EventType.REJECTED);

        return mapReservationToDTO(reservation);
    }

    @Transactional
    public ReservationDTO cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));

        Trip trip = reservation.getTrip();

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            trip.setOccupiedSeats(Math.max(0, trip.getOccupiedSeats() - reservation.getNumberOfSeats()));
            if (trip.getStatus() == TripStatus.FULL && trip.getOccupiedSeats() < trip.getAvailableSeats()) {
                trip.setStatus(TripStatus.OPEN);
            }
            tripRepository.save(trip);
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation = reservationRepository.save(reservation);

        publishReservationEvent(reservation, ReservationEvent.EventType.CANCELLED);
        return mapReservationToDTO(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationDTO> getReservationsByPassenger(Long passengerId) {
        return reservationRepository.findByPassengerIdOrderByCreatedAtDesc(passengerId)
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

    @Transactional(readOnly = true)
    public List<ReservationDTO> getReservationsForDriver(Long driverId) {
        return reservationRepository.findByDriverId(driverId)
                .stream()
                .map(this::mapReservationToDTO)
                .collect(Collectors.toList());
    }

    // ==========================================
    // HELPERS & RABBITMQ
    // ==========================================

    private void publishTripEvent(Trip trip, TripEvent.EventType type) {
        try {
            TripEvent event = TripEvent.builder()
                    .eventType(type)
                    .tripId(trip.getId())
                    .driverId(trip.getDriverId())
                    .driverName(trip.getDriverName())
                    .departureLocation(trip.getDepartureLocation())
                    .destinationLocation(trip.getDestinationLocation())
                    .departureDateTime(trip.getDepartureDateTime())
                    .availableSeats(trip.getAvailableSeats())
                    .occupiedSeats(trip.getOccupiedSeats())
                    .totalCost(trip.getTotalCost())
                    .costPerPassenger(trip.getCostPerPassenger())
                    .distanceKm(trip.getDistance())
                    .co2SavedKg(trip.getCo2SavedKg())
                    .timestamp(LocalDateTime.now())
                    .build();

            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "trip." + type.name().toLowerCase(), event);
            log.info("Evento TripEvent [{}] publicado com sucesso para RabbitMQ", type);
        } catch (Exception e) {
            log.warn("Erro ao publicar evento TripEvent no RabbitMQ (mensageria opcional): {}", e.getMessage());
        }
    }

    private void publishReservationEvent(Reservation res, ReservationEvent.EventType type) {
        try {
            ReservationEvent event = ReservationEvent.builder()
                    .eventType(type)
                    .reservationId(res.getId())
                    .tripId(res.getTrip().getId())
                    .passengerId(res.getPassengerId())
                    .passengerName(res.getPassengerName())
                    .passengerEmail(res.getPassengerEmail())
                    .driverId(res.getTrip().getDriverId())
                    .driverName(res.getTrip().getDriverName())
                    .numberOfSeats(res.getNumberOfSeats())
                    .totalPrice(res.getTotalPrice())
                    .departureLocation(res.getTrip().getDepartureLocation())
                    .destinationLocation(res.getTrip().getDestinationLocation())
                    .departureDateTime(res.getTrip().getDepartureDateTime())
                    .timestamp(LocalDateTime.now())
                    .build();

            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "reservation." + type.name().toLowerCase(), event);
            log.info("Evento ReservationEvent [{}] publicado com sucesso para RabbitMQ", type);
        } catch (Exception e) {
            log.warn("Erro ao publicar evento ReservationEvent no RabbitMQ: {}", e.getMessage());
        }
    }

    private TripDTO mapToDTO(Trip trip) {
        return TripDTO.builder()
                .id(trip.getId())
                .driverId(trip.getDriverId())
                .driverName(trip.getDriverName())
                .vehicleId(trip.getVehicleId())
                .vehicleInfo(trip.getVehicleInfo())
                .departureLocation(trip.getDepartureLocation())
                .destinationLocation(trip.getDestinationLocation())
                .originLatitude(trip.getOriginLatitude())
                .originLongitude(trip.getOriginLongitude())
                .destinationLatitude(trip.getDestinationLatitude())
                .destinationLongitude(trip.getDestinationLongitude())
                .departureDateTime(trip.getDepartureDateTime())
                .availableSeats(trip.getAvailableSeats())
                .occupiedSeats(trip.getOccupiedSeats())
                .totalCost(trip.getTotalCost())
                .costPerPassenger(trip.getCostPerPassenger())
                .estimatedFuelCost(trip.getEstimatedFuelCost())
                .estimatedTolls(trip.getEstimatedTolls())
                .distance(trip.getDistance())
                .co2SavedKg(trip.getCo2SavedKg())
                .description(trip.getDescription())
                .status(trip.getStatus() != null ? trip.getStatus().name() : "OPEN")
                .build();
    }

    private ReservationDTO mapReservationToDTO(Reservation res) {
        return ReservationDTO.builder()
                .id(res.getId())
                .tripId(res.getTrip() != null ? res.getTrip().getId() : null)
                .passengerId(res.getPassengerId())
                .passengerName(res.getPassengerName())
                .passengerEmail(res.getPassengerEmail())
                .numberOfSeats(res.getNumberOfSeats())
                .totalPrice(res.getTotalPrice())
                .status(res.getStatus() != null ? res.getStatus().name() : "PENDING")
                .departureLocation(res.getTrip() != null ? res.getTrip().getDepartureLocation() : null)
                .destinationLocation(res.getTrip() != null ? res.getTrip().getDestinationLocation() : null)
                .departureDateTime(res.getTrip() != null ? res.getTrip().getDepartureDateTime() : null)
                .driverId(res.getTrip() != null ? res.getTrip().getDriverId() : null)
                .driverName(res.getTrip() != null ? res.getTrip().getDriverName() : null)
                .createdAt(res.getCreatedAt())
                .build();
    }

}

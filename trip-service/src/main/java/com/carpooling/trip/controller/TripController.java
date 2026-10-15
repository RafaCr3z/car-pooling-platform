package com.carpooling.trip.controller;

import com.carpooling.trip.dto.CreateReservationRequest;
import com.carpooling.trip.dto.CreateTripRequest;
import com.carpooling.trip.dto.ReservationDTO;
import com.carpooling.trip.dto.TripDTO;
import com.carpooling.trip.service.TripService;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.carpooling.common.security.CurrentUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trips")
@AllArgsConstructor
public class TripController {

    private final TripService tripService;

    private CurrentUser extractCurrentUser(Jwt jwt) {
        return jwt != null ? CurrentUser.from(jwt) : null;
    }

    @PostMapping
    public ResponseEntity<TripDTO> createTrip(
            @RequestBody CreateTripRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        TripDTO trip = tripService.createTrip(request, extractCurrentUser(jwt));
        return ResponseEntity.status(HttpStatus.CREATED).body(trip);
    }

    @GetMapping
    public ResponseEntity<List<TripDTO>> getAllTrips() {
        List<TripDTO> trips = tripService.getAllTrips();
        return ResponseEntity.ok(trips);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripDTO> getTripById(@PathVariable Long id) {
        TripDTO trip = tripService.getTripById(id);
        return ResponseEntity.ok(trip);
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<TripDTO>> getTripsByDriver(@PathVariable Long driverId) {
        List<TripDTO> trips = tripService.getTripsByDriver(driverId);
        return ResponseEntity.ok(trips);
    }

    @GetMapping("/search")
    public ResponseEntity<List<TripDTO>> searchTrips(
            @RequestParam(required = false) String departure,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime departureTime) {
        List<TripDTO> trips = tripService.searchTrips(departure, destination, departureTime);
        return ResponseEntity.ok(trips);
    }

    @GetMapping("/available")
    public ResponseEntity<List<TripDTO>> getAvailableTrips() {
        List<TripDTO> trips = tripService.getAvailableTrips();
        return ResponseEntity.ok(trips);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<TripDTO> updateTripStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @AuthenticationPrincipal Jwt jwt) {
        TripDTO trip = tripService.updateTripStatus(id, status, extractCurrentUser(jwt));
        return ResponseEntity.ok(trip);
    }

    // ==========================================
    // RESERVAS
    // ==========================================

    @PostMapping("/reservations")
    public ResponseEntity<ReservationDTO> createReservation(
            @RequestBody CreateReservationRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        ReservationDTO reservation = tripService.createReservation(request, extractCurrentUser(jwt));
        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }

    @PostMapping("/reservations/{id}/confirm")
    public ResponseEntity<ReservationDTO> confirmReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        ReservationDTO reservation = tripService.confirmReservation(id, extractCurrentUser(jwt));
        return ResponseEntity.ok(reservation);
    }

    @PostMapping("/reservations/{id}/reject")
    public ResponseEntity<ReservationDTO> rejectReservation(
            @PathVariable Long id, 
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal Jwt jwt) {
        String reason = body != null ? body.get("reason") : "Sem justificação";
        ReservationDTO reservation = tripService.rejectReservation(id, reason, extractCurrentUser(jwt));
        return ResponseEntity.ok(reservation);
    }

    @PostMapping("/reservations/{id}/cancel")
    public ResponseEntity<ReservationDTO> cancelReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        ReservationDTO reservation = tripService.cancelReservation(id, extractCurrentUser(jwt));
        return ResponseEntity.ok(reservation);
    }

    @GetMapping("/reservations/passenger/{passengerId}")
    public ResponseEntity<List<ReservationDTO>> getReservationsByPassenger(@PathVariable Long passengerId) {
        List<ReservationDTO> reservations = tripService.getReservationsByPassenger(passengerId);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/reservations/driver/{driverId}")
    public ResponseEntity<List<ReservationDTO>> getReservationsForDriver(@PathVariable Long driverId) {
        List<ReservationDTO> reservations = tripService.getReservationsForDriver(driverId);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/{tripId}/reservations")
    public ResponseEntity<List<ReservationDTO>> getReservationsByTrip(@PathVariable Long tripId) {
        List<ReservationDTO> reservations = tripService.getReservationsByTrip(tripId);
        return ResponseEntity.ok(reservations);
    }

}

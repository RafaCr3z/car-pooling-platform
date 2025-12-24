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

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/trips")
@AllArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    public ResponseEntity<TripDTO> createTrip(@RequestBody CreateTripRequest request) {
        TripDTO trip = tripService.createTrip(request);
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
            @RequestParam String departure,
            @RequestParam String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime departureTime) {
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
            @RequestParam String status) {
        TripDTO trip = tripService.updateTripStatus(id, status);
        return ResponseEntity.ok(trip);
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationDTO> createReservation(@RequestBody CreateReservationRequest request) {
        ReservationDTO reservation = tripService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }

    @GetMapping("/reservations/passenger/{passengerId}")
    public ResponseEntity<List<ReservationDTO>> getReservationsByPassenger(@PathVariable Long passengerId) {
        List<ReservationDTO> reservations = tripService.getReservationsByPassenger(passengerId);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/{tripId}/reservations")
    public ResponseEntity<List<ReservationDTO>> getReservationsByTrip(@PathVariable Long tripId) {
        List<ReservationDTO> reservations = tripService.getReservationsByTrip(tripId);
        return ResponseEntity.ok(reservations);
    }

    @PutMapping("/reservations/{id}/status")
    public ResponseEntity<ReservationDTO> updateReservationStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        ReservationDTO reservation = tripService.updateReservationStatus(id, status);
        return ResponseEntity.ok(reservation);
    }

}

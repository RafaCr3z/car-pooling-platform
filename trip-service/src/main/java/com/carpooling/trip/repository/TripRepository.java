package com.carpooling.trip.repository;

import com.carpooling.trip.entity.Trip;
import com.carpooling.trip.entity.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByDriverId(Long driverId);
    List<Trip> findByStatus(TripStatus status);
    
    @Query("SELECT t FROM Trip t WHERE t.departureLocation = ?1 AND t.destinationLocation = ?2 AND t.departureDateTime >= ?3")
    List<Trip> findTripsBetweenLocations(String departure, String destination, LocalDateTime departureTime);
    
    @Query("SELECT t FROM Trip t WHERE t.status = 'OPEN' AND t.availableSeats > t.occupiedSeats")
    List<Trip> findAvailableTrips();
}

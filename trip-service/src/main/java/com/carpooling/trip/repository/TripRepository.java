package com.carpooling.trip.repository;

import com.carpooling.trip.entity.Trip;
import com.carpooling.trip.entity.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByDriverIdOrderByDepartureDateTimeDesc(Long driverId);
    List<Trip> findByStatus(TripStatus status);
    
    @Query("SELECT t FROM Trip t WHERE " +
           "(:departure IS NULL OR LOWER(t.departureLocation) LIKE LOWER(CONCAT('%', :departure, '%'))) AND " +
           "(:destination IS NULL OR LOWER(t.destinationLocation) LIKE LOWER(CONCAT('%', :destination, '%'))) AND " +
           "(t.departureDateTime >= :departureTime) AND " +
           "t.status = 'OPEN' AND t.availableSeats > t.occupiedSeats " +
           "ORDER BY t.departureDateTime ASC")
    List<Trip> searchAvailableTrips(
            @Param("departure") String departure,
            @Param("destination") String destination,
            @Param("departureTime") LocalDateTime departureTime
    );

    @Query("SELECT t FROM Trip t WHERE t.status = 'OPEN' AND t.availableSeats > t.occupiedSeats ORDER BY t.departureDateTime ASC")
    List<Trip> findAvailableTrips();

    long countByStatus(TripStatus status);
}

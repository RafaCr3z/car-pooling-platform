package com.carpooling.trip.repository;

import com.carpooling.trip.entity.Reservation;
import com.carpooling.trip.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByPassengerIdOrderByCreatedAtDesc(Long passengerId);
    List<Reservation> findByTripId(Long tripId);
    List<Reservation> findByStatus(ReservationStatus status);
    
    @Query("SELECT r FROM Reservation r WHERE r.trip.driverId = :driverId ORDER BY r.createdAt DESC")
    List<Reservation> findByDriverId(Long driverId);
}

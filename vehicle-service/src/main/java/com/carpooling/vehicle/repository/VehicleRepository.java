package com.carpooling.vehicle.repository;

import com.carpooling.vehicle.entity.Vehicle;
import com.carpooling.vehicle.entity.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    
    List<Vehicle> findByOwnerId(Long ownerId);
    
    List<Vehicle> findByStatus(VehicleStatus status);
    
    List<Vehicle> findByOwnerIdAndStatus(Long ownerId, VehicleStatus status);
}

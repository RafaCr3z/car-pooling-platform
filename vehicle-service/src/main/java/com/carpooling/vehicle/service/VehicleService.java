package com.carpooling.vehicle.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.carpooling.vehicle.dto.CreateVehicleRequest;
import com.carpooling.vehicle.dto.VehicleDTO;
import com.carpooling.vehicle.entity.Vehicle;
import com.carpooling.vehicle.entity.VehicleStatus;
import com.carpooling.vehicle.entity.VehicleType;
import com.carpooling.vehicle.repository.VehicleRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class VehicleService {
    
    private final VehicleRepository vehicleRepository;
    
    public VehicleDTO createVehicle(CreateVehicleRequest request) {
        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate(request.getLicensePlate());
        vehicle.setBrand(request.getBrand());
        vehicle.setModel(request.getModel());
        vehicle.setYear(request.getYear());
        vehicle.setType(VehicleType.valueOf(request.getType().toUpperCase()));
        vehicle.setOwnerId(request.getOwnerId());
        vehicle.setPricePerHour(request.getPricePerHour());
        vehicle.setPricePerKm(request.getPricePerKm());
        vehicle.setLocation(request.getLocation());
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicle.setCreatedAt(LocalDateTime.now());
        vehicle.setUpdatedAt(LocalDateTime.now());
        
        Vehicle saved = vehicleRepository.save(vehicle);
        return toDTO(saved);
    }
    
    @Transactional(readOnly = true)
    public VehicleDTO getVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + id));
    }
    
    @Transactional(readOnly = true)
    public List<VehicleDTO> getAllVehicles() {
        return vehicleRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<VehicleDTO> getAvailableVehicles() {
        return vehicleRepository.findByStatus(VehicleStatus.AVAILABLE)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<VehicleDTO> getVehiclesByOwner(Long ownerId) {
        return vehicleRepository.findByOwnerId(ownerId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public void updateVehicleStatus(Long vehicleId, VehicleStatus status) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + vehicleId));
        vehicle.setStatus(status);
        vehicle.setUpdatedAt(LocalDateTime.now());
        vehicleRepository.save(vehicle);
    }
    
    @Transactional(readOnly = true)
    public BigDecimal calculateRentalCost(Long vehicleId, Integer hours, Integer kilometers) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + vehicleId));
        
        BigDecimal hourCost = vehicle.getPricePerHour().multiply(BigDecimal.valueOf(hours));
        BigDecimal kmCost = vehicle.getPricePerKm().multiply(BigDecimal.valueOf(kilometers));
        
        return hourCost.add(kmCost);
    }
    
    private VehicleDTO toDTO(Vehicle vehicle) {
        return VehicleDTO.builder()
                .id(vehicle.getId())
                .licensePlate(vehicle.getLicensePlate())
                .brand(vehicle.getBrand())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .type(vehicle.getType().toString())
                .ownerId(vehicle.getOwnerId())
                .status(vehicle.getStatus().toString())
                .pricePerHour(vehicle.getPricePerHour())
                .pricePerKm(vehicle.getPricePerKm())
                .location(vehicle.getLocation())
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }
}

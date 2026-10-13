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
        VehicleType vType = VehicleType.CAR;
        if (request.getType() != null) {
            try {
                vType = VehicleType.valueOf(request.getType().toUpperCase());
            } catch (Exception ignored) {}
        }

        Vehicle vehicle = Vehicle.builder()
                .licensePlate(request.getLicensePlate().toUpperCase())
                .brand(request.getBrand())
                .model(request.getModel())
                .year(request.getYear())
                .color(request.getColor() != null ? request.getColor() : "Branco")
                .seatCapacity(request.getSeatCapacity() != null ? request.getSeatCapacity() : 4)
                .fuelType(request.getFuelType() != null ? request.getFuelType() : "GASOLINE")
                .fuelConsumptionLPer100Km(request.getFuelConsumptionLPer100Km() != null ? request.getFuelConsumptionLPer100Km() : 5.5)
                .co2EmissionsGPerKm(request.getCo2EmissionsGPerKm() != null ? request.getCo2EmissionsGPerKm() : 115.0)
                .type(vType)
                .ownerId(request.getOwnerId())
                .pricePerHour(request.getPricePerHour() != null ? request.getPricePerHour() : BigDecimal.ZERO)
                .pricePerKm(request.getPricePerKm() != null ? request.getPricePerKm() : BigDecimal.ZERO)
                .location(request.getLocation() != null ? request.getLocation() : "Castelo Branco")
                .status(VehicleStatus.AVAILABLE)
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        Vehicle saved = vehicleRepository.save(vehicle);
        return toDTO(saved);
    }
    
    @Transactional(readOnly = true)
    public VehicleDTO getVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado com id: " + id));
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
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado com id: " + vehicleId));
        vehicle.setStatus(status);
        vehicle.setUpdatedAt(LocalDateTime.now());
        vehicleRepository.save(vehicle);
    }

    public void deleteVehicle(Long id) {
        vehicleRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public BigDecimal calculateRentalCost(Long vehicleId, Integer hours, Integer kilometers) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado com id: " + vehicleId));
        
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
                .color(vehicle.getColor())
                .seatCapacity(vehicle.getSeatCapacity())
                .fuelType(vehicle.getFuelType())
                .fuelConsumptionLPer100Km(vehicle.getFuelConsumptionLPer100Km())
                .co2EmissionsGPerKm(vehicle.getCo2EmissionsGPerKm())
                .type(vehicle.getType() != null ? vehicle.getType().toString() : "CAR")
                .ownerId(vehicle.getOwnerId())
                .status(vehicle.getStatus() != null ? vehicle.getStatus().toString() : "AVAILABLE")
                .pricePerHour(vehicle.getPricePerHour())
                .pricePerKm(vehicle.getPricePerKm())
                .location(vehicle.getLocation())
                .isDefault(vehicle.getIsDefault())
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }
}

package com.carpooling.vehicle.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDTO {
    
    private Long id;
    private String licensePlate;
    private String brand;
    private String model;
    private Integer year;
    private String color;
    private Integer seatCapacity;
    private String fuelType;
    private Double fuelConsumptionLPer100Km;
    private Double co2EmissionsGPerKm;
    private String type;
    private Long ownerId;
    private String status;
    private BigDecimal pricePerHour;
    private BigDecimal pricePerKm;
    private String location;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

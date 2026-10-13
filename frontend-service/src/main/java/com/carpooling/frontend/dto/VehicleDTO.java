package com.carpooling.frontend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    private Long ownerId;
    private Boolean isDefault;
}

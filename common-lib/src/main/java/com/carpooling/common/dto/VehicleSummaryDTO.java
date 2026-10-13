package com.carpooling.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleSummaryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long driverId;
    private String brand;
    private String model;
    private String licensePlate;
    private String color;
    private Integer seatCapacity;
    private String fuelType;
    private Double fuelConsumptionLPer100Km;
    private Double co2EmissionsGPerKm;
}

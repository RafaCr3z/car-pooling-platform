package com.carpooling.vehicle.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateVehicleRequest {
    
    @NotBlank(message = "License plate is required")
    private String licensePlate;
    
    @NotBlank(message = "Brand is required")
    private String brand;
    
    @NotBlank(message = "Model is required")
    private String model;
    
    @NotNull(message = "Year is required")
    @Min(1990)
    @Max(2030)
    private Integer year;
    
    @NotNull(message = "Vehicle type is required")
    private String type;
    
    @NotNull(message = "Owner ID is required")
    private Long ownerId;
    
    @NotNull(message = "Price per hour is required")
    @DecimalMin("0.01")
    private BigDecimal pricePerHour;
    
    @NotNull(message = "Price per km is required")
    @DecimalMin("0.01")
    private BigDecimal pricePerKm;
    
    private String location;
}

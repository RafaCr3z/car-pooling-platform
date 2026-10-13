package com.carpooling.vehicle.dto;

import java.math.BigDecimal;
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
    
    @NotBlank(message = "Matrícula é obrigatória")
    private String licensePlate;
    
    @NotBlank(message = "Marca é obrigatória")
    private String brand;
    
    @NotBlank(message = "Modelo é obrigatório")
    private String model;
    
    @NotNull(message = "Ano é obrigatório")
    @Min(1990)
    @Max(2035)
    private Integer year;

    private String color;
    private Integer seatCapacity;
    private String fuelType;
    private Double fuelConsumptionLPer100Km;
    private Double co2EmissionsGPerKm;
    
    private String type;
    
    @NotNull(message = "ID do proprietário/condutor é obrigatório")
    private Long ownerId;
    
    private BigDecimal pricePerHour;
    private BigDecimal pricePerKm;
    private String location;
    private Boolean isDefault;
}

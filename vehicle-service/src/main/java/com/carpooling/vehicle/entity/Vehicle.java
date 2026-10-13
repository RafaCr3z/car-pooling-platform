package com.carpooling.vehicle.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "vehicles")
public class Vehicle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String licensePlate;
    
    @Column(nullable = false)
    private String brand;
    
    @Column(nullable = false)
    private String model;
    
    @Column(nullable = false)
    private Integer year;

    private String color;

    @Column(nullable = false)
    @Builder.Default
    private Integer seatCapacity = 4;

    private String fuelType; // GASOLINE, DIESEL, ELECTRIC, HYBRID

    @Builder.Default
    private Double fuelConsumptionLPer100Km = 5.5; // L/100km or kWh/100km

    @Builder.Default
    private Double co2EmissionsGPerKm = 115.0; // g CO2 / km
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private VehicleType type = VehicleType.CAR;
    
    @Column(nullable = false)
    private Long ownerId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private VehicleStatus status = VehicleStatus.AVAILABLE;
    
    @Column(nullable = false)
    @Builder.Default
    private BigDecimal pricePerHour = BigDecimal.ZERO;
    
    @Column(nullable = false)
    @Builder.Default
    private BigDecimal pricePerKm = BigDecimal.ZERO;
    
    private String location;

    @Builder.Default
    private Boolean isDefault = false;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (pricePerHour == null) pricePerHour = BigDecimal.ZERO;
        if (pricePerKm == null) pricePerKm = BigDecimal.ZERO;
        if (seatCapacity == null) seatCapacity = 4;
        if (fuelConsumptionLPer100Km == null) fuelConsumptionLPer100Km = 5.5;
        if (co2EmissionsGPerKm == null) co2EmissionsGPerKm = 115.0;
        if (status == null) status = VehicleStatus.AVAILABLE;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

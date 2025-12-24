package com.carpooling.vehicle.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
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
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType type;
    
    @Column(nullable = false)
    private Long ownerId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleStatus status;
    
    @Column(nullable = false)
    private BigDecimal pricePerHour;
    
    @Column(nullable = false)
    private BigDecimal pricePerKm;
    
    private String location;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}

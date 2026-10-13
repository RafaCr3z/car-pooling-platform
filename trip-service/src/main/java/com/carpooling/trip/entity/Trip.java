package com.carpooling.trip.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trips")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long driverId;

    private String driverName;

    private Long vehicleId;

    private String vehicleInfo; // e.g. "Peugeot 208 (12-AB-34)"

    @Column(nullable = false)
    private String departureLocation;

    @Column(nullable = false)
    private String destinationLocation;

    private Double originLatitude;
    private Double originLongitude;
    private Double destinationLatitude;
    private Double destinationLongitude;

    @Column(nullable = false)
    private LocalDateTime departureDateTime;

    @Column(nullable = false)
    private Integer availableSeats;

    @Column(nullable = false)
    @Builder.Default
    private Integer occupiedSeats = 0;

    @Column(nullable = false)
    private BigDecimal totalCost;

    @Column(nullable = false)
    private BigDecimal costPerPassenger;

    @Builder.Default
    private BigDecimal estimatedFuelCost = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal estimatedTolls = BigDecimal.ZERO;

    @Column(nullable = false)
    private Double distance;

    @Builder.Default
    private Double co2SavedKg = 0.0;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TripStatus status = TripStatus.OPEN;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (occupiedSeats == null) occupiedSeats = 0;
        if (status == null) status = TripStatus.OPEN;
        if (estimatedFuelCost == null) estimatedFuelCost = BigDecimal.ZERO;
        if (estimatedTolls == null) estimatedTolls = BigDecimal.ZERO;
        if (co2SavedKg == null) co2SavedKg = 0.0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}

package com.carpooling.trip.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripDTO {
    private Long id;
    private Long driverId;
    private String driverName;
    private Long vehicleId;
    private String vehicleInfo;
    private String departureLocation;
    private String destinationLocation;
    private Double originLatitude;
    private Double originLongitude;
    private Double destinationLatitude;
    private Double destinationLongitude;
    private LocalDateTime departureDateTime;
    private Integer availableSeats;
    private Integer occupiedSeats;
    private BigDecimal totalCost;
    private BigDecimal costPerPassenger;
    private BigDecimal estimatedFuelCost;
    private BigDecimal estimatedTolls;
    private Double distance;
    private Double co2SavedKg;
    private String description;
    private String status;
}

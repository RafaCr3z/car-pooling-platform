package com.carpooling.frontend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
    private String departureDateTime;
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

    public Integer getAvailableSpace() {
        if (availableSeats == null) return 0;
        int occ = occupiedSeats != null ? occupiedSeats : 0;
        return Math.max(0, availableSeats - occ);
    }
}

package com.carpooling.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripResponseDTO {
    private Long id;
    private Long driverId;
    private String driverName;
    private Long vehicleId;
    private String vehicleInfo;
    private String departureLocation;
    private String destinationLocation;
    private LocalDateTime departureDateTime;
    private Integer availableSeats;
    private Integer occupiedSeats;
    private BigDecimal totalCost;
    private BigDecimal costPerPassenger;
    private Double distance;
    private Double co2SavedKg;
    private String status;
}

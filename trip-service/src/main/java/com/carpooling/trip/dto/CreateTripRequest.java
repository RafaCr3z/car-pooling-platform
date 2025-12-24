package com.carpooling.trip.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTripRequest {
    private Long driverId;
    private String departureLocation;
    private String destinationLocation;
    private LocalDateTime departureDateTime;
    private Integer availableSeats;
    private BigDecimal totalCost;
    private Double distance;
}

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
public class AdminMetricsDTO {
    private long totalTrips;
    private long activeTrips;
    private long completedTrips;
    private long cancelledTrips;
    private long totalUsers;
    private long totalDrivers;
    private long totalPassengers;
    private long blockedUsers;
    private BigDecimal totalRevenueTransacted;
    private double totalCo2SavedKg;
    private double treesPlantedEquivalent;
    private double averageUserRating;
    private String systemHealthStatus;
}

package com.carpooling.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

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
    private double treesPlantedEquivalent; // ~22kg CO2 = 1 tree/year
    private double averageUserRating;
    private String systemHealthStatus;
}

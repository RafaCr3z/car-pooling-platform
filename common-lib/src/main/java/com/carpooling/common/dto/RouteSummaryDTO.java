package com.carpooling.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteSummaryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String origin;
    private String destination;
    private Double originLatitude;
    private Double originLongitude;
    private Double destinationLatitude;
    private Double destinationLongitude;
    private Double distanceKm;
    private Integer durationMinutes;
    private BigDecimal estimatedFuelCost;
    private BigDecimal estimatedTolls;
    private String routeGeometry; // GeoJSON or Polyline representation for maps
}

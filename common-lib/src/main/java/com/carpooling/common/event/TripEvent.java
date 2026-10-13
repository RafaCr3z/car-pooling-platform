package com.carpooling.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum EventType {
        CREATED,
        STARTED,
        COMPLETED,
        CANCELLED
    }

    private EventType eventType;
    private Long tripId;
    private Long driverId;
    private String driverName;
    private String driverEmail;
    private String departureLocation;
    private String destinationLocation;
    private LocalDateTime departureDateTime;
    private Integer availableSeats;
    private Integer occupiedSeats;
    private BigDecimal totalCost;
    private BigDecimal costPerPassenger;
    private Double distanceKm;
    private Double co2SavedKg;
    private List<Long> passengerIds;
    private LocalDateTime timestamp;
}

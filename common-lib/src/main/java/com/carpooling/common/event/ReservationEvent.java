package com.carpooling.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum EventType {
        REQUESTED,
        CONFIRMED,
        REJECTED,
        CANCELLED
    }

    private EventType eventType;
    private Long reservationId;
    private Long tripId;
    private Long passengerId;
    private String passengerName;
    private String passengerEmail;
    private Long driverId;
    private String driverName;
    private String driverEmail;
    private Integer numberOfSeats;
    private BigDecimal totalPrice;
    private String departureLocation;
    private String destinationLocation;
    private LocalDateTime departureDateTime;
    private LocalDateTime timestamp;
}

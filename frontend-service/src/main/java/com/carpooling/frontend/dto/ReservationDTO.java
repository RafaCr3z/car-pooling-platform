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
public class ReservationDTO {
    private Long id;
    private Long tripId;
    private Long passengerId;
    private String passengerName;
    private String passengerEmail;
    private Long driverId;
    private String driverName;
    private String departureLocation;
    private String destinationLocation;
    private String departureDateTime;
    private Integer numberOfSeats;
    private BigDecimal totalPrice;
    private String status; // PENDING, CONFIRMED, REJECTED, CANCELLED
    private String rejectionReason;
    private String createdAt;
}

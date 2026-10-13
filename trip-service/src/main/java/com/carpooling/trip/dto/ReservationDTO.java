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
public class ReservationDTO {
    private Long id;
    private Long tripId;
    private Long passengerId;
    private String passengerName;
    private String passengerEmail;
    private Integer numberOfSeats;
    private BigDecimal totalPrice;
    private String status;
    private String departureLocation;
    private String destinationLocation;
    private LocalDateTime departureDateTime;
    private Long driverId;
    private String driverName;
    private LocalDateTime createdAt;
}

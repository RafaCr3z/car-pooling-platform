package com.carpooling.trip.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDTO {
    private Long id;
    private Long tripId;
    private Long passengerId;
    private Integer numberOfSeats;
    private BigDecimal totalPrice;
    private String status;
}

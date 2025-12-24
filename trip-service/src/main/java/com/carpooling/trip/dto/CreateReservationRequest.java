package com.carpooling.trip.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReservationRequest {
    private Long tripId;
    private Long passengerId;
    private Integer numberOfSeats = 1;
}

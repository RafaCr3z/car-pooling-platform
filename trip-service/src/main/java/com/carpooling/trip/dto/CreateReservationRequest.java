package com.carpooling.trip.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReservationRequest {
    private Long tripId;
    private Long passengerId;
    private String passengerName;
    private String passengerEmail;
    private String passengerPhone;
    @Builder.Default
    private Integer numberOfSeats = 1;
}

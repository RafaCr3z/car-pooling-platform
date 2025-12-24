package com.carpooling.frontend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDTO {
    private Long id;
    private Long tripId;
    private Integer numberOfSeats;
    private Double totalPrice;
    private String status;
}

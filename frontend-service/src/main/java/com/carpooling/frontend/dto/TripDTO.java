package com.carpooling.frontend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripDTO {
    private Long id;
    private String departureLocation;
    private String destinationLocation;
    private String departureDateTime;
    private Integer availableSeats;
    private Integer occupiedSeats;
    private Double costPerPassenger;

    public Integer getAvailableSpace() {
        return availableSeats - occupiedSeats;
    }
}

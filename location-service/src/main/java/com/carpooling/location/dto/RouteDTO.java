package com.carpooling.location.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteDTO {
    
    @NotBlank(message = "Origin address is required")
    private String origin;
    
    @NotNull(message = "Origin latitude is required")
    private BigDecimal originLatitude;
    
    @NotNull(message = "Origin longitude is required")
    private BigDecimal originLongitude;
    
    @NotBlank(message = "Destination address is required")
    private String destination;
    
    @NotNull(message = "Destination latitude is required")
    private BigDecimal destLatitude;
    
    @NotNull(message = "Destination longitude is required")
    private BigDecimal destLongitude;
    
    private Double distance; // in km
    private Long estimatedTime; // in minutes
}

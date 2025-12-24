package com.carpooling.payment.dto;

import com.carpooling.payment.entity.PaymentMethod;
import com.carpooling.payment.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentRequest {
    
    @NotNull(message = "Trip ID is required")
    private Long tripId;
    
    @NotNull(message = "Passenger ID is required")
    private Long passengerId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod method;
    
    private String description;
}

package com.carpooling.payment.dto;

import com.carpooling.payment.entity.PaymentMethod;
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
    
    @NotNull(message = "ID da viagem é obrigatório")
    private Long tripId;
    
    private Long reservationId;
    
    @NotNull(message = "ID do passageiro é obrigatório")
    private Long passengerId;

    private Long driverId;
    
    @NotNull(message = "Montante é obrigatório")
    @DecimalMin(value = "0.01", message = "Montante deve ser superior a 0")
    private BigDecimal amount;
    
    private PaymentMethod method;
    
    private String description;
}

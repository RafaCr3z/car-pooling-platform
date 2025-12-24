package com.carpooling.payment.dto;

import com.carpooling.payment.entity.PaymentMethod;
import com.carpooling.payment.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {
    
    private Long id;
    private Long tripId;
    private Long passengerId;
    private BigDecimal amount;
    private PaymentStatus status;
    private PaymentMethod method;
    private String transactionId;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
}

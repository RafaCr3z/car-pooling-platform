package com.carpooling.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum PaymentStatus {
        PENDING,
        COMPLETED,
        FAILED,
        REFUNDED
    }

    private Long paymentId;
    private Long tripId;
    private Long reservationId;
    private Long payerId;
    private String payerName;
    private Long receiverId;
    private BigDecimal amount;
    private String paymentMethod;
    private PaymentStatus status;
    private String transactionReference;
    private LocalDateTime timestamp;
}

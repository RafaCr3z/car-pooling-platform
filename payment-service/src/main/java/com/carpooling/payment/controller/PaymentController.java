package com.carpooling.payment.controller;

import com.carpooling.payment.dto.CreatePaymentRequest;
import com.carpooling.payment.dto.PaymentDTO;
import com.carpooling.payment.entity.PaymentStatus;
import com.carpooling.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @PostMapping
    public ResponseEntity<PaymentDTO> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        PaymentDTO payment = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    @GetMapping
    public ResponseEntity<List<PaymentDTO>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/total")
    public ResponseEntity<Map<String, Object>> getTotalTransacted() {
        BigDecimal total = paymentService.getTotalTransactedAmount();
        return ResponseEntity.ok(Map.of("totalAmount", total));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PaymentDTO> getPaymentById(@PathVariable Long id) {
        PaymentDTO payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(payment);
    }
    
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<PaymentDTO> getPaymentByTransactionId(@PathVariable String transactionId) {
        PaymentDTO payment = paymentService.getPaymentByTransactionId(transactionId);
        return ResponseEntity.ok(payment);
    }
    
    @GetMapping("/trip/{tripId}")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByTrip(@PathVariable Long tripId) {
        List<PaymentDTO> payments = paymentService.getPaymentsByTripId(tripId);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByPassenger(@PathVariable Long passengerId) {
        List<PaymentDTO> payments = paymentService.getPaymentsByPassengerId(passengerId);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        List<PaymentDTO> payments = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(payments);
    }
    
    @PostMapping("/{id}/complete")
    public ResponseEntity<Void> completePayment(@PathVariable Long id) {
        paymentService.completePayment(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/fail")
    public ResponseEntity<Void> failPayment(@PathVariable Long id) {
        paymentService.failPayment(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/refund")
    public ResponseEntity<Void> refundPayment(@PathVariable Long id) {
        paymentService.refundPayment(id);
        return ResponseEntity.noContent().build();
    }
}

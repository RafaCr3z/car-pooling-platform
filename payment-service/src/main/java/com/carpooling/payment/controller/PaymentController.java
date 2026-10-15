package com.carpooling.payment.controller;

import com.carpooling.common.security.CurrentUser;
import com.carpooling.payment.dto.CreatePaymentRequest;
import com.carpooling.payment.dto.PaymentDTO;
import com.carpooling.payment.entity.PaymentStatus;
import com.carpooling.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    public ResponseEntity<PaymentDTO> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        if (jwt != null) {
            CurrentUser user = CurrentUser.from(jwt);
            if (!user.isAdmin() && request.getPassengerId() != null && !user.is(request.getPassengerId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Não pode emitir pagamentos em nome de outro passageiro");
            }
        }
        PaymentDTO payment = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    @GetMapping
    public ResponseEntity<List<PaymentDTO>> getAllPayments(@AuthenticationPrincipal Jwt jwt) {
        if (jwt != null) {
            CurrentUser.from(jwt).requireAdmin();
        }
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/total")
    public ResponseEntity<Map<String, Object>> getTotalTransacted(@AuthenticationPrincipal Jwt jwt) {
        if (jwt != null) {
            CurrentUser.from(jwt).requireAdmin();
        }
        BigDecimal total = paymentService.getTotalTransactedAmount();
        return ResponseEntity.ok(Map.of("totalAmount", total));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PaymentDTO> getPaymentById(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        PaymentDTO payment = paymentService.getPaymentById(id);
        if (jwt != null) {
            CurrentUser user = CurrentUser.from(jwt);
            if (!user.isAdmin() && !user.is(payment.getPassengerId()) && !user.is(payment.getDriverId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sem permissão para consultar o pagamento de outro utilizador");
            }
        }
        return ResponseEntity.ok(payment);
    }
    
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<PaymentDTO> getPaymentByTransactionId(
            @PathVariable String transactionId,
            @AuthenticationPrincipal Jwt jwt) {
        PaymentDTO payment = paymentService.getPaymentByTransactionId(transactionId);
        if (jwt != null) {
            CurrentUser user = CurrentUser.from(jwt);
            if (!user.isAdmin() && !user.is(payment.getPassengerId()) && !user.is(payment.getDriverId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sem permissão para consultar o pagamento de outro utilizador");
            }
        }
        return ResponseEntity.ok(payment);
    }
    
    @GetMapping("/trip/{tripId}")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByTrip(@PathVariable Long tripId) {
        List<PaymentDTO> payments = paymentService.getPaymentsByTripId(tripId);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByPassenger(
            @PathVariable Long passengerId,
            @AuthenticationPrincipal Jwt jwt) {
        if (jwt != null) {
            CurrentUser.from(jwt).requireSelfOrAdmin(passengerId);
        }
        List<PaymentDTO> payments = paymentService.getPaymentsByPassengerId(passengerId);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByStatus(
            @PathVariable PaymentStatus status,
            @AuthenticationPrincipal Jwt jwt) {
        if (jwt != null) {
            CurrentUser.from(jwt).requireAdmin();
        }
        List<PaymentDTO> payments = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(payments);
    }
    
    @PostMapping("/{id}/complete")
    public ResponseEntity<Void> completePayment(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        if (jwt != null) {
            CurrentUser.from(jwt).requireAdmin();
        }
        paymentService.completePayment(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/fail")
    public ResponseEntity<Void> failPayment(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        if (jwt != null) {
            CurrentUser.from(jwt).requireAdmin();
        }
        paymentService.failPayment(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/refund")
    public ResponseEntity<Void> refundPayment(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        if (jwt != null) {
            CurrentUser.from(jwt).requireAdmin();
        }
        paymentService.refundPayment(id);
        return ResponseEntity.noContent().build();
    }
}

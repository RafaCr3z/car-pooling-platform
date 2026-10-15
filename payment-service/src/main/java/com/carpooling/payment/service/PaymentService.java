package com.carpooling.payment.service;

import com.carpooling.payment.dto.CreatePaymentRequest;
import com.carpooling.payment.dto.PaymentDTO;
import com.carpooling.payment.entity.Payment;
import com.carpooling.payment.entity.PaymentMethod;
import com.carpooling.payment.entity.PaymentStatus;
import com.carpooling.payment.repository.PaymentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    
    public PaymentDTO createPayment(CreatePaymentRequest request) {
        if (request.getReservationId() != null) {
            Optional<Payment> existing = paymentRepository.findByReservationId(request.getReservationId());
            if (existing.isPresent()) {
                log.info("Pagamento já existente para reserva ID={}: retornando registo ID={} (idempotente)",
                        request.getReservationId(), existing.get().getId());
                return toDTO(existing.get());
            }
        }

        PaymentMethod method = request.getMethod() != null ? request.getMethod() : PaymentMethod.MBWAY;
        String txnPrefix = method == PaymentMethod.MBWAY ? "MBWAY-" : "CARD-";

        Payment payment = Payment.builder()
                .tripId(request.getTripId())
                .reservationId(request.getReservationId())
                .passengerId(request.getPassengerId())
                .driverId(request.getDriverId())
                .amount(request.getAmount())
                .method(method)
                .status(PaymentStatus.COMPLETED) // Simulação aprovada de imediato
                .transactionId(txnPrefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .description(request.getDescription() != null ? request.getDescription() : "Partilha de despesas de viagem")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();
        
        Payment saved = paymentRepository.save(payment);
        log.info("Pagamento registado e aprovado com sucesso: ID={}, Txn={}", saved.getId(), saved.getTransactionId());
        return toDTO(saved);
    }
    
    @Transactional(readOnly = true)
    public PaymentDTO getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado com id: " + id));
    }
    
    @Transactional(readOnly = true)
    public PaymentDTO getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado com transaction id: " + transactionId));
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByTripId(Long tripId) {
        return paymentRepository.findByTripId(tripId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByPassengerId(Long passengerId) {
        return paymentRepository.findByPassengerId(passengerId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalTransactedAmount() {
        return paymentRepository.findByStatus(PaymentStatus.COMPLETED).stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public void completePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado com id: " + paymentId));
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setCompletedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);
    }

    public void failPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado com id: " + paymentId));
        payment.setStatus(PaymentStatus.FAILED);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);
    }
    
    public void refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado com id: " + paymentId));
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);
    }
    
    private PaymentDTO toDTO(Payment payment) {
        return PaymentDTO.builder()
                .id(payment.getId())
                .tripId(payment.getTripId())
                .reservationId(payment.getReservationId())
                .passengerId(payment.getPassengerId())
                .driverId(payment.getDriverId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .method(payment.getMethod())
                .transactionId(payment.getTransactionId())
                .description(payment.getDescription())
                .createdAt(payment.getCreatedAt())
                .completedAt(payment.getCompletedAt())
                .build();
    }
}

package com.carpooling.payment.service;

import com.carpooling.payment.dto.CreatePaymentRequest;
import com.carpooling.payment.dto.PaymentDTO;
import com.carpooling.payment.entity.Payment;
import com.carpooling.payment.entity.PaymentStatus;
import com.carpooling.payment.repository.PaymentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    
    public PaymentDTO createPayment(CreatePaymentRequest request) {
        Payment payment = new Payment();
        payment.setTripId(request.getTripId());
        payment.setPassengerId(request.getPassengerId());
        payment.setAmount(request.getAmount());
        payment.setMethod(request.getMethod());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setDescription(request.getDescription());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        
        Payment saved = paymentRepository.save(payment);
        return toDTO(saved);
    }
    
    @Transactional(readOnly = true)
    public PaymentDTO getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
    }
    
    @Transactional(readOnly = true)
    public PaymentDTO getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Payment not found with transaction id: " + transactionId));
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
    
    public void completePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setCompletedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);
    }
    
    public void failPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));
        payment.setStatus(PaymentStatus.FAILED);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);
    }
    
    public void refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);
    }
    
    private PaymentDTO toDTO(Payment payment) {
        return PaymentDTO.builder()
                .id(payment.getId())
                .tripId(payment.getTripId())
                .passengerId(payment.getPassengerId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .method(payment.getMethod())
                .transactionId(payment.getTransactionId())
                .description(payment.getDescription())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .completedAt(payment.getCompletedAt())
                .build();
    }
}

package com.carpooling.payment.repository;

import com.carpooling.payment.entity.Payment;
import com.carpooling.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByTransactionId(String transactionId);
    
    Optional<Payment> findByReservationId(Long reservationId);
    
    List<Payment> findByTripId(Long tripId);
    
    List<Payment> findByPassengerId(Long passengerId);
    
    List<Payment> findByStatus(PaymentStatus status);
}

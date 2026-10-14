package com.carpooling.payment;

import com.carpooling.payment.dto.CreatePaymentRequest;
import com.carpooling.payment.dto.PaymentDTO;
import com.carpooling.payment.entity.Payment;
import com.carpooling.payment.entity.PaymentMethod;
import com.carpooling.payment.entity.PaymentStatus;
import com.carpooling.payment.repository.PaymentRepository;
import com.carpooling.payment.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    @DisplayName("Deve processar pagamento via MBWay com prefixo MBWAY- e estado COMPLETED")
    void testCreatePayment_MBWay() {
        CreatePaymentRequest req = CreatePaymentRequest.builder()
                .tripId(1L)
                .reservationId(5L)
                .passengerId(2L)
                .driverId(1L)
                .amount(new BigDecimal("15.50"))
                .method(PaymentMethod.MBWAY)
                .description("Boleia Castelo Branco - Lisboa")
                .build();

        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(100L);
            return p;
        });

        PaymentDTO result = paymentService.createPayment(req);

        assertNotNull(result);
        assertEquals(new BigDecimal("15.50"), result.getAmount());
        assertEquals(PaymentMethod.MBWAY, result.getMethod());
        assertEquals(PaymentStatus.COMPLETED, result.getStatus());
        assertTrue(result.getTransactionId().startsWith("MBWAY-"));
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("Deve processar pagamento via Cartão com prefixo CARD-")
    void testCreatePayment_Card() {
        CreatePaymentRequest req = CreatePaymentRequest.builder()
                .tripId(2L)
                .reservationId(6L)
                .passengerId(3L)
                .driverId(1L)
                .amount(new BigDecimal("22.00"))
                .method(PaymentMethod.CREDIT_CARD)
                .build();

        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(101L);
            return p;
        });

        PaymentDTO result = paymentService.createPayment(req);

        assertNotNull(result);
        assertEquals(PaymentMethod.CREDIT_CARD, result.getMethod());
        assertTrue(result.getTransactionId().startsWith("CARD-"));
    }

    @Test
    @DisplayName("Deve recuperar pagamento por transactionId")
    void testGetPaymentByTransactionId() {
        Payment p = Payment.builder()
                .id(1L)
                .transactionId("MBWAY-ABCD1234")
                .amount(new BigDecimal("12.00"))
                .method(PaymentMethod.MBWAY)
                .status(PaymentStatus.COMPLETED)
                .build();

        when(paymentRepository.findByTransactionId("MBWAY-ABCD1234")).thenReturn(Optional.of(p));

        PaymentDTO result = paymentService.getPaymentByTransactionId("MBWAY-ABCD1234");

        assertNotNull(result);
        assertEquals("MBWAY-ABCD1234", result.getTransactionId());
        assertEquals(new BigDecimal("12.00"), result.getAmount());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o pagamento não for encontrado")
    void testGetPaymentById_NotFound_ThrowsException() {
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> paymentService.getPaymentById(999L));
        assertTrue(ex.getMessage().contains("não encontrado"));
    }
}

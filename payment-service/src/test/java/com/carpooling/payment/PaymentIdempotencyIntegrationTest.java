package com.carpooling.payment;

import com.carpooling.payment.entity.Payment;
import com.carpooling.payment.entity.PaymentMethod;
import com.carpooling.payment.entity.PaymentStatus;
import com.carpooling.payment.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:payment_integration_db;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDER=HIGH",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@DisplayName("Testes de Integração - Idempotência e Persistência Real de Pagamentos (JPA / H2)")
class PaymentIdempotencyIntegrationTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Integração: Persistência bem-sucedida de Pagamento e Consulta Idempotente por TransactionId")
    void testSuccessfulPaymentPersistenceAndLookup() {
        Payment payment = Payment.builder()
                .tripId(10L)
                .reservationId(50L)
                .passengerId(200L)
                .driverId(100L)
                .amount(new BigDecimal("15.50"))
                .status(PaymentStatus.PENDING)
                .method(PaymentMethod.MBWAY)
                .transactionId("TX-IDEMP-20261015-001")
                .description("Pagamento viagem Castelo Branco -> Lisboa via MBWay")
                .build();

        Payment saved = paymentRepository.save(payment);
        assertNotNull(saved.getId(), "O ID do pagamento deve ser gerado pelo banco");

        entityManager.flush();
        entityManager.clear();

        Optional<Payment> found = paymentRepository.findByTransactionId("TX-IDEMP-20261015-001");
        assertTrue(found.isPresent(), "O pagamento deve ser recuperado pelo transactionId único");
        assertEquals(new BigDecimal("15.50"), found.get().getAmount());
        assertEquals(PaymentStatus.PENDING, found.get().getStatus());
        assertEquals(PaymentMethod.MBWAY, found.get().getMethod());
        assertNotNull(found.get().getCreatedAt());
    }

    @Test
    @DisplayName("Integração: Tentativa de inserção com transactionId duplicado deve falhar com DataIntegrityViolationException (Anti-Double Spending)")
    void testDuplicateTransactionIdThrowsDataIntegrityViolationException() {
        String sharedTransactionId = "TX-IDEMP-DUPLICATE-999";

        Payment payment1 = Payment.builder()
                .tripId(12L)
                .reservationId(52L)
                .passengerId(202L)
                .driverId(102L)
                .amount(new BigDecimal("22.00"))
                .status(PaymentStatus.COMPLETED)
                .method(PaymentMethod.CREDIT_CARD)
                .transactionId(sharedTransactionId)
                .description("Primeiro débito autorizado")
                .build();

        paymentRepository.save(payment1);
        entityManager.flush();

        Payment payment2 = Payment.builder()
                .tripId(12L)
                .reservationId(52L)
                .passengerId(202L)
                .driverId(102L)
                .amount(new BigDecimal("22.00"))
                .status(PaymentStatus.PENDING)
                .method(PaymentMethod.CREDIT_CARD)
                .transactionId(sharedTransactionId)
                .description("Tentativa duplicada ou reenvio de rede")
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            paymentRepository.save(payment2);
            entityManager.flush();
        }, "A constraint UNIQUE em transaction_id deve impedir estritamente duplicados na base de dados");
    }

    @Test
    @DisplayName("Integração: Transição de Estado do Pagamento (PENDING -> COMPLETED) e Auditoria de Timestamps")
    void testPaymentLifecycleStatusTransitions() {
        Payment payment = Payment.builder()
                .tripId(15L)
                .reservationId(55L)
                .passengerId(205L)
                .driverId(105L)
                .amount(new BigDecimal("8.75"))
                .status(PaymentStatus.PENDING)
                .method(PaymentMethod.WALLET)
                .transactionId("TX-STATUS-TRANSITION-001")
                .description("Pagamento pendente de aprovação")
                .build();

        Payment saved = paymentRepository.save(payment);
        entityManager.flush();
        entityManager.clear();

        Payment toUpdate = paymentRepository.findById(saved.getId()).orElseThrow();
        assertEquals(PaymentStatus.PENDING, toUpdate.getStatus());
        assertNull(toUpdate.getCompletedAt());

        // Concluir pagamento
        toUpdate.setStatus(PaymentStatus.COMPLETED);
        toUpdate.setCompletedAt(LocalDateTime.now());
        paymentRepository.save(toUpdate);

        entityManager.flush();
        entityManager.clear();

        Payment reloaded = paymentRepository.findById(saved.getId()).orElseThrow();
        assertEquals(PaymentStatus.COMPLETED, reloaded.getStatus());
        assertNotNull(reloaded.getCompletedAt(), "Timestamp de conclusão deve estar preenchido");
        assertNotNull(reloaded.getUpdatedAt());
    }

    @Test
    @DisplayName("Integração: Consultas relacionais de pagamentos por TripId e PassengerId")
    void testFindPaymentsByTripAndPassenger() {
        Payment p1 = Payment.builder()
                .tripId(20L)
                .reservationId(61L)
                .passengerId(301L)
                .amount(new BigDecimal("10.00"))
                .transactionId("TX-REL-001")
                .build();

        Payment p2 = Payment.builder()
                .tripId(20L)
                .reservationId(62L)
                .passengerId(302L)
                .amount(new BigDecimal("10.00"))
                .transactionId("TX-REL-002")
                .build();

        paymentRepository.save(p1);
        paymentRepository.save(p2);
        entityManager.flush();
        entityManager.clear();

        List<Payment> tripPayments = paymentRepository.findByTripId(20L);
        assertEquals(2, tripPayments.size(), "Devem existir 2 pagamentos associados à viagem 20");

        List<Payment> passengerPayments = paymentRepository.findByPassengerId(301L);
        assertEquals(1, passengerPayments.size(), "Deve existir 1 pagamento para o passageiro 301");
        assertEquals("TX-REL-001", passengerPayments.get(0).getTransactionId());
    }
}

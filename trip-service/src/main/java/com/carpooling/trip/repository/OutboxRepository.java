package com.carpooling.trip.repository;

import com.carpooling.trip.entity.OutboxEvent;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * Seleciona os eventos pendentes com bloqueio pessimista e SKIP LOCKED (timeout = -2 no Hibernate).
     * Garante que múltiplas réplicas do trip-service em execução concorrente nunca processem
     * os mesmos eventos em simultâneo, prevenindo eventos duplicados no broker RabbitMQ.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")})
    @Query("SELECT o FROM OutboxEvent o WHERE o.status = :status ORDER BY o.createdAt ASC")
    List<OutboxEvent> findTopPendingEvents(@Param("status") String status, Pageable pageable);
}

package com.carpooling.admin.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String adminUsername;

    @Column(nullable = false)
    private String action; // USER_BLOCKED, USER_UNBLOCKED, TRIP_CANCELLED, SYSTEM_METRICS_AUDIT

    private String targetEntity; // USER, TRIP, PAYMENT

    private Long targetId;

    @Column(length = 1000)
    private String details;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}

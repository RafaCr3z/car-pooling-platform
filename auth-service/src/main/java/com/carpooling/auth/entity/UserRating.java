package com.carpooling.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_ratings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long raterId;

    @Column(nullable = false)
    private String raterName;

    @Column(nullable = false)
    private Long ratedUserId;

    private Long tripId;

    @Column(nullable = false)
    private Integer score; // 1 to 5

    @Column(length = 1000)
    private String comment;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

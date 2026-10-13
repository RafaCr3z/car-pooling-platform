package com.carpooling.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRatingDTO {
    private Long id;
    private Long raterId;
    private String raterName;
    private Long ratedUserId;
    private Long tripId;
    private Integer score;
    private String comment;
    private LocalDateTime createdAt;
}

package com.carpooling.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateUserRequest {
    private Long raterId;
    private Long tripId;
    private Integer score; // 1 - 5
    private String comment;
}

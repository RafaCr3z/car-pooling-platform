package com.carpooling.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String role;
    private Double rating;
    private Integer ratingCount;
    private Boolean isBlocked;
}

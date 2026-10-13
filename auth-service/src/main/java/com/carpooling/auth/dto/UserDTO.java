package com.carpooling.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String bio;
    private String role;
    private List<String> roles;
    private Double rating;
    private Integer ratingCount;
    private Boolean isActive;
    private Boolean isBlocked;
}

package com.carpooling.frontend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLogDTO {
    private Long id;
    private String adminUsername;
    private String action;
    private String targetEntity;
    private Long targetId;
    private String details;
    private String timestamp;
}

package com.carpooling.frontend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private Long userId;
    private String userEmail;
    private String title;
    private String message;
    private String type;
    private Long relatedTripId;
    private Boolean isRead;
    private String createdAt;
}

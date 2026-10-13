package com.carpooling.frontend.controller;

import com.carpooling.frontend.dto.NotificationDTO;
import com.carpooling.frontend.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications/api")
@AllArgsConstructor
public class NotificationApiController {

    private final NotificationService notificationService;

    @GetMapping("/unread-count")
    public Map<String, Long> getUnreadCount(HttpSession session) {
        String token = (String) session.getAttribute("token");
        Long userId = (Long) session.getAttribute("userId");
        if (token == null || userId == null) {
            return Map.of("unreadCount", 0L);
        }
        long count = notificationService.getUnreadCount(userId, token);
        return Map.of("unreadCount", count);
    }

    @GetMapping("/list")
    public List<NotificationDTO> getNotifications(HttpSession session) {
        String token = (String) session.getAttribute("token");
        Long userId = (Long) session.getAttribute("userId");
        if (token == null || userId == null) {
            return Collections.emptyList();
        }
        return notificationService.getUserNotifications(userId, token);
    }

    @PostMapping("/{id}/read")
    public Map<String, String> markAsRead(@PathVariable Long id, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token != null) {
            notificationService.markAsRead(id, token);
        }
        return Map.of("status", "ok");
    }
}

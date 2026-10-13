package com.carpooling.frontend.service;

import com.carpooling.frontend.dto.NotificationDTO;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class NotificationService {

    private final WebClient webClient;

    public List<NotificationDTO> getUserNotifications(Long userId, String token) {
        try {
            if (userId == null) return Collections.emptyList();
            String response = webClient.get()
                    .uri("/api/notifications/user/" + userId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            List<NotificationDTO> list = new ArrayList<>();
            JsonArray array = JsonParser.parseString(response).getAsJsonArray();
            for (var el : array) {
                JsonObject o = el.getAsJsonObject();
                list.add(NotificationDTO.builder()
                        .id(optLong(o, "id"))
                        .userId(optLong(o, "userId"))
                        .userEmail(optString(o, "userEmail"))
                        .title(optString(o, "title"))
                        .message(optString(o, "message"))
                        .type(optString(o, "type"))
                        .relatedTripId(optLong(o, "relatedTripId"))
                        .isRead(optBoolean(o, "isRead"))
                        .createdAt(optString(o, "createdAt"))
                        .build());
            }
            return list;
        } catch (Exception e) {
            log.warn("Erro ao buscar notificações: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public long getUnreadCount(Long userId, String token) {
        try {
            if (userId == null) return 0;
            String response = webClient.get()
                    .uri("/api/notifications/user/" + userId + "/unread-count")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonObject o = JsonParser.parseString(response).getAsJsonObject();
            return o.has("unreadCount") ? o.get("unreadCount").getAsLong() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public void markAsRead(Long notificationId, String token) {
        try {
            webClient.put()
                    .uri("/api/notifications/" + notificationId + "/read")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.warn("Erro ao marcar notificação como lida: {}", e.getMessage());
        }
    }

    private String optString(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : null;
    }
    private Long optLong(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsLong() : null;
    }
    private Boolean optBoolean(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() && o.get(key).getAsBoolean();
    }
}

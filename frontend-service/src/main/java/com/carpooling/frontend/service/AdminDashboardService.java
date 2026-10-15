package com.carpooling.frontend.service;

import com.carpooling.common.dto.UserSummaryDTO;
import com.carpooling.common.dto.AdminMetricsDTO;
import com.carpooling.frontend.dto.AdminLogDTO;
import com.carpooling.frontend.dto.TripDTO;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class AdminDashboardService {

    private final WebClient webClient;
    private final Gson gson = new Gson();

    public AdminMetricsDTO getMetrics(String token) {
        try {
            String response = webClient.get()
                    .uri("/api/admin/metrics")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonObject o = JsonParser.parseString(response).getAsJsonObject();
            return AdminMetricsDTO.builder()
                    .totalTrips(optLong(o, "totalTrips"))
                    .activeTrips(optLong(o, "activeTrips"))
                    .completedTrips(optLong(o, "completedTrips"))
                    .cancelledTrips(optLong(o, "cancelledTrips"))
                    .totalUsers(optLong(o, "totalUsers"))
                    .totalDrivers(optLong(o, "totalDrivers"))
                    .totalPassengers(optLong(o, "totalPassengers"))
                    .blockedUsers(optLong(o, "blockedUsers"))
                    .totalRevenueTransacted(optBigDecimal(o, "totalRevenueTransacted"))
                    .totalCo2SavedKg(optDouble(o, "totalCo2SavedKg"))
                    .treesPlantedEquivalent(optDouble(o, "treesPlantedEquivalent"))
                    .averageUserRating(optDouble(o, "averageUserRating"))
                    .systemHealthStatus(optString(o, "systemHealthStatus"))
                    .build();
        } catch (Exception e) {
            log.error("Erro ao buscar métricas de administração: {}", e.getMessage());
            return AdminMetricsDTO.builder()
                    .systemHealthStatus("OFFLINE")
                    .totalRevenueTransacted(BigDecimal.ZERO)
                    .build();
        }
    }

    public List<UserSummaryDTO> getUsers(String token) {
        try {
            String response = webClient.get()
                    .uri("/api/admin/users")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            List<UserSummaryDTO> users = new ArrayList<>();
            JsonArray array = JsonParser.parseString(response).getAsJsonArray();
            for (var el : array) {
                JsonObject o = el.getAsJsonObject();
                users.add(UserSummaryDTO.builder()
                        .id(optLong(o, "id"))
                        .email(optString(o, "email"))
                        .fullName(optString(o, "fullName"))
                        .phoneNumber(optString(o, "phoneNumber"))
                        .role(optString(o, "role"))
                        .rating(optDouble(o, "rating"))
                        .ratingCount(optInt(o, "ratingCount"))
                        .isBlocked(optBoolean(o, "isBlocked"))
                        .build());
            }
            return users;
        } catch (Exception e) {
            log.error("Erro ao buscar utilizadores no admin: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<AdminLogDTO> getLogs(String token) {
        try {
            String response = webClient.get()
                    .uri("/api/admin/logs")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            List<AdminLogDTO> logs = new ArrayList<>();
            JsonArray array = JsonParser.parseString(response).getAsJsonArray();
            for (var el : array) {
                JsonObject o = el.getAsJsonObject();
                logs.add(AdminLogDTO.builder()
                        .id(optLong(o, "id"))
                        .adminUsername(optString(o, "adminUsername"))
                        .action(optString(o, "action"))
                        .targetEntity(optString(o, "targetEntity"))
                        .targetId(optLong(o, "targetId"))
                        .details(optString(o, "details"))
                        .timestamp(optString(o, "timestamp"))
                        .build());
            }
            return logs;
        } catch (Exception e) {
            log.error("Erro ao buscar logs: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public void blockUser(Long userId, String adminUsername, String reason, String token) {
        webClient.put()
                .uri("/api/admin/users/" + userId + "/block?adminUsername=" + (adminUsername != null ? adminUsername : "admin"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("reason", reason != null ? reason : "Conduta inapropriada"))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public void unblockUser(Long userId, String adminUsername, String token) {
        webClient.put()
                .uri("/api/admin/users/" + userId + "/unblock?adminUsername=" + (adminUsername != null ? adminUsername : "admin"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public void cancelTrip(Long tripId, String adminUsername, String reason, String token) {
        webClient.put()
                .uri("/api/admin/trips/" + tripId + "/cancel?adminUsername=" + (adminUsername != null ? adminUsername : "admin"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("reason", reason != null ? reason : "Cancelamento administrativo"))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private String optString(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : null;
    }
    private Long optLong(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsLong() : null;
    }
    private Integer optInt(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsInt() : 0;
    }
    private Double optDouble(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsDouble() : 0.0;
    }
    private Boolean optBoolean(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() && o.get(key).getAsBoolean();
    }
    private BigDecimal optBigDecimal(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? BigDecimal.valueOf(o.get(key).getAsDouble()) : BigDecimal.ZERO;
    }
}

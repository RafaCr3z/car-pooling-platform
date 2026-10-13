package com.carpooling.frontend.service;

import com.carpooling.frontend.dto.LoginResponseDTO;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class AuthService {

    private final WebClient webClient;

    public LoginResponseDTO login(String email, String password) {
        try {
            String requestBody = String.format(
                    "{\"email\":\"%s\",\"password\":\"%s\"}",
                    email, password
            );

            String response = webClient.post()
                    .uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            String token = json.get("token").getAsString();
            Long userId = json.has("userId") && !json.get("userId").isJsonNull() ? json.get("userId").getAsLong() : null;
            String userEmail = json.has("email") && !json.get("email").isJsonNull() ? json.get("email").getAsString() : email;
            String fullName = json.has("fullName") && !json.get("fullName").isJsonNull() ? json.get("fullName").getAsString() : email;
            String role = json.has("role") && !json.get("role").isJsonNull() ? json.get("role").getAsString() : "PASSENGER";

            List<String> roles = new ArrayList<>();
            if (json.has("roles") && json.get("roles").isJsonArray()) {
                json.get("roles").getAsJsonArray().forEach(r -> roles.add(r.getAsString()));
            } else {
                roles.add(role);
            }

            log.info("Login bem-sucedido: email={} userId={} role={}", userEmail, userId, role);

            return LoginResponseDTO.builder()
                    .token(token)
                    .userId(userId)
                    .email(userEmail)
                    .fullName(fullName)
                    .role(role)
                    .roles(roles)
                    .build();

        } catch (Exception e) {
            log.error("Erro ao fazer login: {}", e.getMessage());
            throw new RuntimeException("Erro ao fazer login: " + e.getMessage(), e);
        }
    }

    public void register(String email, String password, String fullName, 
                        String phoneNumber, String role) {
        try {
            String requestBody = String.format(
                    "{\"email\":\"%s\",\"password\":\"%s\",\"fullName\":\"%s\"," +
                    "\"phoneNumber\":\"%s\",\"role\":\"%s\"}",
                    email, password, fullName, phoneNumber, role
            );

            webClient.post()
                    .uri("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Utilizador registado com sucesso: {}", email);

        } catch (Exception e) {
            log.error("Erro ao registar utilizador: {}", e.getMessage());
            throw new RuntimeException("Erro ao registar utilizador", e);
        }
    }
}

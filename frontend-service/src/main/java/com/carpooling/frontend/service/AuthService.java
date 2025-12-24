package com.carpooling.frontend.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
@AllArgsConstructor
public class AuthService {

    private final WebClient webClient;

    /**
     * Faz login via API Gateway
     */
    public String login(String email, String password) {
        try {
            String requestBody = String.format(
                    "{\"email\":\"%s\",\"password\":\"%s\"}",
                    email, password
            );

            String response = webClient.post()
                    .uri("http://localhost:8080/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Extrair token da resposta
            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
            String token = jsonResponse.get("token").getAsString();

            log.info("Token gerado com sucesso para: {}", email);
            return token;

        } catch (Exception e) {
            log.error("Erro ao fazer login: {}", e.getMessage());
            throw new RuntimeException("Erro ao fazer login", e);
        }
    }

    /**
     * Registar novo utilizador via API Gateway
     */
    public void register(String email, String password, String fullName, 
                        String phoneNumber, String role) {
        try {
            String requestBody = String.format(
                    "{\"email\":\"%s\",\"password\":\"%s\",\"fullName\":\"%s\"," +
                    "\"phoneNumber\":\"%s\",\"role\":\"%s\"}",
                    email, password, fullName, phoneNumber, role
            );

            webClient.post()
                    .uri("http://localhost:8080/api/auth/register")
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

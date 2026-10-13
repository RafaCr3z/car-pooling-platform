package com.carpooling.admin.client.fallback;

import com.carpooling.admin.client.AuthServiceClient;
import com.carpooling.common.dto.UserSummaryDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class AuthClientFallback implements AuthServiceClient {

    @Override
    public List<UserSummaryDTO> getAllUsers() {
        log.warn("Fallback ativado: Auth Service indisponível para getAllUsers");
        return Collections.emptyList();
    }

    @Override
    public Map<String, Object> blockUser(Long id) {
        log.warn("Fallback ativado: Auth Service indisponível para blockUser id={}", id);
        return Map.of("success", false, "message", "Auth service indisponível (Circuit Breaker)");
    }

    @Override
    public Map<String, Object> unblockUser(Long id) {
        log.warn("Fallback ativado: Auth Service indisponível para unblockUser id={}", id);
        return Map.of("success", false, "message", "Auth service indisponível (Circuit Breaker)");
    }
}

package com.carpooling.admin.client;

import com.carpooling.admin.client.fallback.AuthClientFallback;
import com.carpooling.common.dto.UserSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;
import java.util.Map;

@FeignClient(name = "auth-service", fallback = AuthClientFallback.class)
public interface AuthServiceClient {

    @GetMapping("/api/auth/users")
    List<UserSummaryDTO> getAllUsers();

    @PutMapping("/api/auth/user/{id}/block")
    Map<String, Object> blockUser(@PathVariable("id") Long id);

    @PutMapping("/api/auth/user/{id}/unblock")
    Map<String, Object> unblockUser(@PathVariable("id") Long id);
}

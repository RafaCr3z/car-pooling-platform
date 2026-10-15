package com.carpooling.admin.controller;

import com.carpooling.admin.dto.AdminMetricsDTO;
import com.carpooling.admin.dto.TripResponseDTO;
import com.carpooling.admin.entity.AdminLog;
import com.carpooling.admin.service.AdminService;
import com.carpooling.common.dto.UserSummaryDTO;
import com.carpooling.common.privacy.PiiMasker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/metrics")
    public ResponseEntity<AdminMetricsDTO> getSystemMetrics() {
        return ResponseEntity.ok(adminService.getSystemMetrics());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserSummaryDTO>> getAllUsers() {
        // Conformidade com RGPD (Minimização de Dados - Artigo 5.º, n.º 1, alínea c do RGPD):
        // Listagens administrativas nunca devem expor emails e telefones completos em texto limpo.
        List<UserSummaryDTO> users = adminService.getAllUsers();
        List<UserSummaryDTO> maskedUsers = users.stream()
                .map(PiiMasker::mask)
                .collect(Collectors.toList());
        return ResponseEntity.ok(maskedUsers);
    }

    @PutMapping("/users/{userId}/block")
    public ResponseEntity<Map<String, Object>> blockUser(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Email", required = false, defaultValue = "admin@carpooling.pt") String adminUsername,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : "Conduta inapropriada";
        return ResponseEntity.ok(adminService.blockUser(adminUsername, userId, reason));
    }

    @PutMapping("/users/{userId}/unblock")
    public ResponseEntity<Map<String, Object>> unblockUser(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Email", required = false, defaultValue = "admin@carpooling.pt") String adminUsername) {
        return ResponseEntity.ok(adminService.unblockUser(adminUsername, userId));
    }

    @GetMapping("/trips")
    public ResponseEntity<List<TripResponseDTO>> getAllTrips() {
        return ResponseEntity.ok(adminService.getAllTrips());
    }

    @PutMapping("/trips/{tripId}/cancel")
    public ResponseEntity<TripResponseDTO> cancelTrip(
            @PathVariable Long tripId,
            @RequestHeader(value = "X-User-Email", required = false, defaultValue = "admin@carpooling.pt") String adminUsername,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : "Intervenção administrativa";
        return ResponseEntity.ok(adminService.cancelTrip(adminUsername, tripId, reason));
    }

    @GetMapping("/logs")
    public ResponseEntity<List<AdminLog>> getLogs() {
        return ResponseEntity.ok(adminService.getRecentLogs());
    }
}

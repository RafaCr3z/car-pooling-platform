package com.carpooling.admin.controller;

import com.carpooling.admin.dto.AdminMetricsDTO;
import com.carpooling.admin.dto.TripResponseDTO;
import com.carpooling.admin.entity.AdminLog;
import com.carpooling.admin.service.AdminService;
import com.carpooling.common.dto.UserSummaryDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/metrics")
    public ResponseEntity<AdminMetricsDTO> getSystemMetrics() {
        return ResponseEntity.ok(adminService.getSystemMetrics());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserSummaryDTO>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{userId}/block")
    public ResponseEntity<Map<String, Object>> blockUser(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "admin") String adminUsername,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : "Conduta inapropriada";
        return ResponseEntity.ok(adminService.blockUser(adminUsername, userId, reason));
    }

    @PutMapping("/users/{userId}/unblock")
    public ResponseEntity<Map<String, Object>> unblockUser(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "admin") String adminUsername) {
        return ResponseEntity.ok(adminService.unblockUser(adminUsername, userId));
    }

    @GetMapping("/trips")
    public ResponseEntity<List<TripResponseDTO>> getAllTrips() {
        return ResponseEntity.ok(adminService.getAllTrips());
    }

    @PutMapping("/trips/{tripId}/cancel")
    public ResponseEntity<TripResponseDTO> cancelTrip(
            @PathVariable Long tripId,
            @RequestParam(required = false, defaultValue = "admin") String adminUsername,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : "Intervenção administrativa";
        return ResponseEntity.ok(adminService.cancelTrip(adminUsername, tripId, reason));
    }

    @GetMapping("/logs")
    public ResponseEntity<List<AdminLog>> getLogs() {
        return ResponseEntity.ok(adminService.getRecentLogs());
    }
}

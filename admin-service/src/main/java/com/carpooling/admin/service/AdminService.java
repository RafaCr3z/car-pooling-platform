package com.carpooling.admin.service;

import com.carpooling.admin.client.AuthServiceClient;
import com.carpooling.admin.client.PaymentServiceClient;
import com.carpooling.admin.client.TripServiceClient;
import com.carpooling.admin.dto.AdminMetricsDTO;
import com.carpooling.admin.dto.TripResponseDTO;
import com.carpooling.admin.entity.AdminLog;
import com.carpooling.admin.repository.AdminLogRepository;
import com.carpooling.common.dto.UserSummaryDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final AuthServiceClient authServiceClient;
    private final TripServiceClient tripServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final AdminLogRepository adminLogRepository;

    public AdminMetricsDTO getSystemMetrics() {
        List<UserSummaryDTO> users = authServiceClient.getAllUsers();
        List<TripResponseDTO> trips = tripServiceClient.getAllTrips();
        Map<String, Object> paymentTotal = paymentServiceClient.getTotalTransacted();

        long totalUsers = users.size();
        long totalDrivers = users.stream().filter(u -> "DRIVER".equalsIgnoreCase(u.getRole())).count();
        long totalPassengers = users.stream().filter(u -> "PASSENGER".equalsIgnoreCase(u.getRole())).count();
        long blockedUsers = users.stream().filter(u -> Boolean.TRUE.equals(u.getIsBlocked())).count();

        double avgRating = users.stream()
                .filter(u -> u.getRating() != null && u.getRating() > 0)
                .mapToDouble(UserSummaryDTO::getRating)
                .average()
                .orElse(5.0);

        long totalTrips = trips.size();
        long activeTrips = trips.stream().filter(t -> "OPEN".equalsIgnoreCase(t.getStatus()) || "IN_PROGRESS".equalsIgnoreCase(t.getStatus()) || "SCHEDULED".equalsIgnoreCase(t.getStatus()) || "ACTIVE".equalsIgnoreCase(t.getStatus()) || "STARTED".equalsIgnoreCase(t.getStatus())).count();
        long completedTrips = trips.stream().filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus())).count();
        long cancelledTrips = trips.stream().filter(t -> "CANCELLED".equalsIgnoreCase(t.getStatus())).count();

        double totalCo2Saved = trips.stream()
                .filter(t -> t.getCo2SavedKg() != null)
                .mapToDouble(TripResponseDTO::getCo2SavedKg)
                .sum();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        if (paymentTotal != null && paymentTotal.get("totalAmount") != null) {
            Object amt = paymentTotal.get("totalAmount");
            if (amt instanceof BigDecimal) {
                totalRevenue = (BigDecimal) amt;
            } else if (amt instanceof Number) {
                totalRevenue = BigDecimal.valueOf(((Number) amt).doubleValue());
            }
        }

        double treesEquivalent = totalCo2Saved / 22.0;

        return AdminMetricsDTO.builder()
                .totalTrips(totalTrips)
                .activeTrips(activeTrips)
                .completedTrips(completedTrips)
                .cancelledTrips(cancelledTrips)
                .totalUsers(totalUsers)
                .totalDrivers(totalDrivers)
                .totalPassengers(totalPassengers)
                .blockedUsers(blockedUsers)
                .totalRevenueTransacted(totalRevenue)
                .totalCo2SavedKg(Math.round(totalCo2Saved * 10.0) / 10.0)
                .treesPlantedEquivalent(Math.round(treesEquivalent * 10.0) / 10.0)
                .averageUserRating(Math.round(avgRating * 100.0) / 100.0)
                .systemHealthStatus("OPERATIONAL")
                .build();
    }

    public List<UserSummaryDTO> getAllUsers() {
        return authServiceClient.getAllUsers();
    }

    public List<TripResponseDTO> getAllTrips() {
        return tripServiceClient.getAllTrips();
    }

    public List<AdminLog> getRecentLogs() {
        return adminLogRepository.findTop50ByOrderByTimestampDesc();
    }

    public Map<String, Object> blockUser(String adminUsername, Long userId, String reason) {
        Map<String, Object> response = authServiceClient.blockUser(userId);
        AdminLog logEntry = AdminLog.builder()
                .adminUsername(adminUsername != null ? adminUsername : "admin")
                .action("USER_BLOCKED")
                .targetEntity("USER")
                .targetId(userId)
                .details("Utilizador bloqueado por moderador. Motivo: " + (reason != null ? reason : "Conduta inapropriada"))
                .build();
        adminLogRepository.save(logEntry);
        return response;
    }

    public Map<String, Object> unblockUser(String adminUsername, Long userId) {
        Map<String, Object> response = authServiceClient.unblockUser(userId);
        AdminLog logEntry = AdminLog.builder()
                .adminUsername(adminUsername != null ? adminUsername : "admin")
                .action("USER_UNBLOCKED")
                .targetEntity("USER")
                .targetId(userId)
                .details("Utilizador desbloqueado por moderador.")
                .build();
        adminLogRepository.save(logEntry);
        return response;
    }

    public TripResponseDTO cancelTrip(String adminUsername, Long tripId, String reason) {
        TripResponseDTO cancelled = tripServiceClient.updateTripStatus(tripId, "CANCELLED");
        AdminLog logEntry = AdminLog.builder()
                .adminUsername(adminUsername != null ? adminUsername : "admin")
                .action("TRIP_CANCELLED")
                .targetEntity("TRIP")
                .targetId(tripId)
                .details("Viagem cancelada administrativamente. Motivo: " + (reason != null ? reason : "Intervenção administrativa"))
                .build();
        adminLogRepository.save(logEntry);
        return cancelled;
    }
}

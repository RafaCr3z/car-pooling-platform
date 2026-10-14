package com.carpooling.admin;

import com.carpooling.admin.client.AuthServiceClient;
import com.carpooling.admin.client.PaymentServiceClient;
import com.carpooling.admin.client.TripServiceClient;
import com.carpooling.admin.dto.AdminMetricsDTO;
import com.carpooling.admin.dto.TripResponseDTO;
import com.carpooling.admin.entity.AdminLog;
import com.carpooling.admin.repository.AdminLogRepository;
import com.carpooling.admin.service.AdminService;
import com.carpooling.common.dto.UserSummaryDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AuthServiceClient authServiceClient;

    @Mock
    private TripServiceClient tripServiceClient;

    @Mock
    private PaymentServiceClient paymentServiceClient;

    @Mock
    private AdminLogRepository adminLogRepository;

    @InjectMocks
    private AdminService adminService;

    @Test
    @DisplayName("Deve agregar métricas globais e calcular equivalência de árvores salvas a partir do CO2")
    void testGetSystemMetrics_CalculatesAggregatesAndTreesEquivalence() {
        UserSummaryDTO u1 = UserSummaryDTO.builder().id(1L).role("DRIVER").rating(4.8).isBlocked(false).build();
        UserSummaryDTO u2 = UserSummaryDTO.builder().id(2L).role("PASSENGER").rating(5.0).isBlocked(false).build();
        when(authServiceClient.getAllUsers()).thenReturn(List.of(u1, u2));

        TripResponseDTO t1 = TripResponseDTO.builder().id(10L).status("ACTIVE").co2SavedKg(22.0).build();
        TripResponseDTO t2 = TripResponseDTO.builder().id(11L).status("COMPLETED").co2SavedKg(44.0).build();
        when(tripServiceClient.getAllTrips()).thenReturn(List.of(t1, t2));

        when(paymentServiceClient.getTotalTransacted()).thenReturn(Map.of("totalAmount", new BigDecimal("120.00")));

        AdminMetricsDTO metrics = adminService.getSystemMetrics();

        assertNotNull(metrics);
        assertEquals(2, metrics.getTotalUsers());
        assertEquals(1, metrics.getTotalDrivers());
        assertEquals(1, metrics.getTotalPassengers());
        assertEquals(2, metrics.getTotalTrips());
        assertEquals(1, metrics.getActiveTrips());
        assertEquals(1, metrics.getCompletedTrips());
        assertEquals(66.0, metrics.getTotalCo2SavedKg());
        // 66.0 kg / 22.0 = 3.0 árvores
        assertEquals(3.0, metrics.getTreesPlantedEquivalent());
        assertEquals(4.9, metrics.getAverageUserRating());
        assertEquals(new BigDecimal("120.00"), metrics.getTotalRevenueTransacted());
        assertEquals("OPERATIONAL", metrics.getSystemHealthStatus());
    }

    @Test
    @DisplayName("Deve bloquear utilizador e registar entrada de auditoria nos logs de administração")
    void testBlockUser_CreatesAuditLog() {
        when(authServiceClient.blockUser(5L)).thenReturn(Map.of("status", "success", "message", "Utilizador bloqueado"));
        when(adminLogRepository.save(any(AdminLog.class))).thenAnswer(i -> i.getArgument(0));

        Map<String, Object> result = adminService.blockUser("admin_gestor", 5L, "Comportamento inadequado");

        assertNotNull(result);
        assertEquals("success", result.get("status"));
        verify(authServiceClient).blockUser(5L);
        verify(adminLogRepository).save(argThat(log ->
                "admin_gestor".equals(log.getAdminUsername()) &&
                "USER_BLOCKED".equals(log.getAction()) &&
                log.getDetails().contains("Comportamento inadequado")
        ));
    }
}

package com.carpooling.admin;

import com.carpooling.admin.client.AuthServiceClient;
import com.carpooling.admin.client.PaymentServiceClient;
import com.carpooling.admin.client.TripServiceClient;
import com.carpooling.common.dto.AdminMetricsDTO;
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

    @Test
    @DisplayName("Deve desbloquear utilizador e registar ação de auditoria USER_UNBLOCKED")
    void testUnblockUser_CreatesAuditLog() {
        when(authServiceClient.unblockUser(5L)).thenReturn(Map.of("status", "success", "message", "Utilizador desbloqueado"));
        when(adminLogRepository.save(any(AdminLog.class))).thenAnswer(i -> i.getArgument(0));

        Map<String, Object> result = adminService.unblockUser("admin_gestor", 5L);

        assertNotNull(result);
        assertEquals("success", result.get("status"));
        verify(authServiceClient).unblockUser(5L);
        verify(adminLogRepository).save(argThat(log ->
                "admin_gestor".equals(log.getAdminUsername()) &&
                "USER_UNBLOCKED".equals(log.getAction())
        ));
    }

    @Test
    @DisplayName("Deve obter histórico de logs recentes de administração")
    void testGetRecentLogs() {
        AdminLog log1 = AdminLog.builder().id(1L).action("USER_BLOCKED").adminUsername("admin").build();
        when(adminLogRepository.findTop50ByOrderByTimestampDesc()).thenReturn(List.of(log1));

        List<AdminLog> logs = adminService.getRecentLogs();

        assertEquals(1, logs.size());
        assertEquals("USER_BLOCKED", logs.get(0).getAction());
        verify(adminLogRepository).findTop50ByOrderByTimestampDesc();
    }

    @Test
    @DisplayName("Deve delegar listagem de todos os utilizadores ao AuthServiceClient")
    void testGetAllUsers() {
        UserSummaryDTO u = UserSummaryDTO.builder().id(1L).email("user@test.pt").build();
        when(authServiceClient.getAllUsers()).thenReturn(List.of(u));

        List<UserSummaryDTO> users = adminService.getAllUsers();

        assertEquals(1, users.size());
        assertEquals("user@test.pt", users.get(0).getEmail());
    }

    @Test
    @DisplayName("Deve delegar listagem de todas as viagens ao TripServiceClient")
    void testGetAllTrips() {
        TripResponseDTO t = TripResponseDTO.builder().id(100L).status("OPEN").build();
        when(tripServiceClient.getAllTrips()).thenReturn(List.of(t));

        List<TripResponseDTO> trips = adminService.getAllTrips();

        assertEquals(1, trips.size());
        assertEquals(100L, trips.get(0).getId());
    }
}


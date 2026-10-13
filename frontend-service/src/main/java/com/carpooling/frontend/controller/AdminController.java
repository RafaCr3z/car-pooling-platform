package com.carpooling.frontend.controller;

import com.carpooling.frontend.dto.AdminLogDTO;
import com.carpooling.frontend.dto.AdminMetricsDTO;
import com.carpooling.common.dto.UserSummaryDTO;
import com.carpooling.frontend.service.AdminDashboardService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@Slf4j
@AllArgsConstructor
public class AdminController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    public String adminDashboard(HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        String role = (String) session.getAttribute("role");

        if (token == null) return "redirect:/login";

        AdminMetricsDTO metrics = adminDashboardService.getMetrics(token);
        List<UserSummaryDTO> users = adminDashboardService.getUsers(token);
        List<AdminLogDTO> logs = adminDashboardService.getLogs(token);

        model.addAttribute("metrics", metrics);
        model.addAttribute("users", users);
        model.addAttribute("logs", logs);
        model.addAttribute("adminRole", role);

        return "admin/admin";
    }

    @PostMapping("/users/{userId}/block")
    public String blockUser(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "Conduta inapropriada") String reason,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String token = (String) session.getAttribute("token");
        String email = (String) session.getAttribute("email");
        if (token == null) return "redirect:/login";

        try {
            adminDashboardService.blockUser(userId, email, reason, token);
            redirectAttributes.addFlashAttribute("success", "Utilizador #" + userId + " bloqueado com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao bloquear utilizador: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/users/{userId}/unblock")
    public String unblockUser(
            @PathVariable Long userId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String token = (String) session.getAttribute("token");
        String email = (String) session.getAttribute("email");
        if (token == null) return "redirect:/login";

        try {
            adminDashboardService.unblockUser(userId, email, token);
            redirectAttributes.addFlashAttribute("success", "Utilizador #" + userId + " desbloqueado com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao desbloquear utilizador: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/trips/{tripId}/cancel")
    public String cancelTrip(
            @PathVariable Long tripId,
            @RequestParam(required = false, defaultValue = "Intervenção administrativa") String reason,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String token = (String) session.getAttribute("token");
        String email = (String) session.getAttribute("email");
        if (token == null) return "redirect:/login";

        try {
            adminDashboardService.cancelTrip(tripId, email, reason, token);
            redirectAttributes.addFlashAttribute("success", "Viagem #" + tripId + " cancelada administrativamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao cancelar viagem: " + e.getMessage());
        }
        return "redirect:/admin";
    }
}

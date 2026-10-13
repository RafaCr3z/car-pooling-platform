package com.carpooling.frontend.controller;

import com.carpooling.frontend.dto.LoginResponseDTO;
import com.carpooling.frontend.service.AuthService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Slf4j
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            log.info("Tentando login para: {}", email);
            LoginResponseDTO response = authService.login(email, password);

            // Guardar na sessão HTTP
            session.setAttribute("token", response.getToken());
            session.setAttribute("userId", response.getUserId());
            session.setAttribute("email", response.getEmail());
            session.setAttribute("fullName", response.getFullName());
            session.setAttribute("role", response.getRole());
            session.setAttribute("roles", response.getRoles());

            log.info("Login bem-sucedido para {} (id={}, role={})", email, response.getUserId(), response.getRole());
            redirectAttributes.addFlashAttribute("success", "Bem-vindo(a), " + response.getFullName() + "!");

            if ("ADMIN".equalsIgnoreCase(response.getRole())) {
                return "redirect:/admin";
            }
            return "redirect:/dashboard";

        } catch (Exception e) {
            log.error("Erro no login: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Email ou palavra-passe incorretos.");
            return "redirect:/login";
        }
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String fullName,
            @RequestParam String phoneNumber,
            @RequestParam String role,
            RedirectAttributes redirectAttributes) {

        try {
            log.info("Registando utilizador: {}", email);
            authService.register(email, password, fullName, phoneNumber, role);

            redirectAttributes.addFlashAttribute("success", "Conta criada com sucesso! Faça login para continuar.");
            return "redirect:/login";

        } catch (Exception e) {
            log.error("Erro no registo: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Erro ao criar conta: " + e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        log.info("Logout realizado");
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "Sessão terminada com sucesso.");
        return "redirect:/";
    }
}

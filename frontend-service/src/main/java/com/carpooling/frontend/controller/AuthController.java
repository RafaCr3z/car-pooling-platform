package com.carpooling.frontend.controller;

import com.carpooling.frontend.service.AuthService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
@Slf4j
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Página de Login
     */
    @GetMapping("/login")
    public String loginPage() {
        log.info("Acessando página de login");
        return "auth/login";
    }

    /**
     * Processa o Login
     */
    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            log.info("Tentando login para: {}", email);
            String token = authService.login(email, password);

            // Guardar token na sessão
            session.setAttribute("token", token);
            session.setAttribute("email", email);

            log.info("Login bem sucedido para: {}", email);
            redirectAttributes.addFlashAttribute("success", "Login realizado com sucesso!");
            return "redirect:/dashboard";

        } catch (Exception e) {
            log.error("Erro no login: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Email ou password incorretos");
            return "redirect:/login";
        }
    }

    /**
     * Página de Registo
     */
    @GetMapping("/register")
    public String registerPage() {
        log.info("Acessando página de registo");
        return "auth/register";
    }

    /**
     * Processa o Registo
     */
    @PostMapping("/register")
    public String register(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String fullName,
            @RequestParam String phoneNumber,
            @RequestParam String role,
            RedirectAttributes redirectAttributes) {

        try {
            log.info("Registando novo utilizador: {}", email);
            authService.register(email, password, fullName, phoneNumber, role);

            log.info("Registo bem sucedido para: {}", email);
            redirectAttributes.addFlashAttribute("success", "Registo realizado com sucesso! Faça login para continuar.");
            return "redirect:/login";

        } catch (Exception e) {
            log.error("Erro no registo: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Erro no registo: " + e.getMessage());
            return "redirect:/register";
        }
    }

    /**
     * Logout
     */
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        log.info("Logout realizado");
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "Logout realizado com sucesso");
        return "redirect:/";
    }

}

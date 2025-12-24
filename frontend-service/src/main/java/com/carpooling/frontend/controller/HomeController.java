package com.carpooling.frontend.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
@Slf4j
@AllArgsConstructor
public class HomeController {

    /**
     * Página inicial (Home)
     */
    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        log.info("Acessando página inicial");

        // Verificar se utilizador está autenticado
        String email = (String) session.getAttribute("email");
        if (email != null) {
            model.addAttribute("isAuthenticated", true);
            model.addAttribute("userEmail", email);
        }

        return "index";
    }

    /**
     * Página de Dashboard (após login)
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String email = (String) session.getAttribute("email");
        String token = (String) session.getAttribute("token");

        if (email == null || token == null) {
            log.warn("Tentativa de acesso ao dashboard sem autenticação");
            return "redirect:/login";
        }

        log.info("Acessando dashboard de: {}", email);
        model.addAttribute("userEmail", email);

        return "dashboard/dashboard";
    }

    /**
     * Página de Perfil
     */
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        String email = (String) session.getAttribute("email");
        String token = (String) session.getAttribute("token");

        if (email == null || token == null) {
            return "redirect:/login";
        }

        log.info("Acessando perfil de: {}", email);
        model.addAttribute("userEmail", email);

        return "dashboard/profile";
    }

}

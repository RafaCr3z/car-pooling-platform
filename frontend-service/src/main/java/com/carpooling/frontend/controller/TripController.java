package com.carpooling.frontend.controller;

import com.carpooling.frontend.service.TripService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
@Slf4j
@AllArgsConstructor
public class TripController {

    private final TripService tripService;

    /**
     * Página para criar nova viagem
     */
    @GetMapping("/trips/create")
    public String createTripPage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("email");
        String token = (String) session.getAttribute("token");

        if (email == null || token == null) {
            return "redirect:/login";
        }

        log.info("Acessando página de criar viagem para: {}", email);
        return "trips/create-trip";
    }

    /**
     * Processa criação de viagem
     */
    @PostMapping("/trips/create")
    public String createTrip(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam String departureDateTime,
            @RequestParam Integer availableSeats,
            @RequestParam Double totalCost,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute("email");
        String token = (String) session.getAttribute("token");

        if (email == null || token == null) {
            return "redirect:/login";
        }

        try {
            log.info("Criando viagem de {} para {} por: {}", origin, destination, email);

            tripService.createTrip(origin, destination, departureDateTime, 
                    availableSeats, totalCost, token);

            redirectAttributes.addFlashAttribute("success", "Viagem criada com sucesso!");
            return "redirect:/trips/my-trips";

        } catch (Exception e) {
            log.error("Erro ao criar viagem: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Erro ao criar viagem: " + e.getMessage());
            return "redirect:/trips/create";
        }
    }

    /**
     * Página de pesquisa de viagens
     */
    @GetMapping("/trips/search")
    public String searchTripsPage(
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) String date,
            HttpSession session,
            Model model) {

        String email = (String) session.getAttribute("email");
        String token = (String) session.getAttribute("token");

        if (email == null || token == null) {
            return "redirect:/login";
        }

        try {
            log.info("Pesquisando viagens: {} -> {} em {}", origin, destination, date);

            // Chamar API para buscar viagens
            if (origin != null && destination != null) {
                var trips = tripService.searchTrips(origin, destination, token);
                model.addAttribute("trips", trips);
                model.addAttribute("resultados", true);
            } else {
                model.addAttribute("resultados", false);
            }

            model.addAttribute("origin", origin);
            model.addAttribute("destination", destination);
            model.addAttribute("date", date);

            return "trips/search-trips";

        } catch (Exception e) {
            log.error("Erro na pesquisa: {}", e.getMessage());
            model.addAttribute("error", "Erro na pesquisa: " + e.getMessage());
            return "trips/search-trips";
        }
    }

    /**
     * Página com as minhas viagens
     */
    @GetMapping("/trips/my-trips")
    public String myTripsPage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("email");
        String token = (String) session.getAttribute("token");

        if (email == null || token == null) {
            return "redirect:/login";
        }

        try {
            log.info("Carregando minhas viagens para: {}", email);

            var myTrips = tripService.getMyTrips(token);
            model.addAttribute("trips", myTrips);

            return "trips/my-trips";

        } catch (Exception e) {
            log.error("Erro ao carregar viagens: {}", e.getMessage());
            model.addAttribute("error", "Erro ao carregar viagens");
            return "trips/my-trips";
        }
    }

    /**
     * Página com as minhas reservas
     */
    @GetMapping("/trips/my-reservations")
    public String myReservationsPage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("email");
        String token = (String) session.getAttribute("token");

        if (email == null || token == null) {
            return "redirect:/login";
        }

        try {
            log.info("Carregando minhas reservas para: {}", email);

            var reservations = tripService.getMyReservations(token);
            model.addAttribute("reservations", reservations);

            return "trips/my-reservations";

        } catch (Exception e) {
            log.error("Erro ao carregar reservas: {}", e.getMessage());
            model.addAttribute("error", "Erro ao carregar reservas");
            return "trips/my-reservations";
        }
    }

    /**
     * Fazer reserva de viagem
     */
    @PostMapping("/trips/reserve")
    public String makeReservation(
            @RequestParam Long tripId,
            @RequestParam Integer seats,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute("email");
        String token = (String) session.getAttribute("token");

        if (email == null || token == null) {
            return "redirect:/login";
        }

        try {
            log.info("Fazendo reserva da viagem {} para: {}", tripId, email);

            tripService.makeReservation(tripId, seats, token);

            redirectAttributes.addFlashAttribute("success", "Reserva realizada com sucesso!");
            return "redirect:/trips/my-reservations";

        } catch (Exception e) {
            log.error("Erro ao fazer reserva: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Erro ao fazer reserva: " + e.getMessage());
            return "redirect:/trips/search";
        }
    }

}

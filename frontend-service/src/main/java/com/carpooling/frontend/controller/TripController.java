package com.carpooling.frontend.controller;

import com.carpooling.frontend.dto.ReservationDTO;
import com.carpooling.frontend.dto.TripDTO;
import com.carpooling.frontend.dto.VehicleDTO;
import com.carpooling.frontend.service.TripService;
import com.carpooling.frontend.service.VehicleService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@AllArgsConstructor
public class TripController {

    private final TripService tripService;
    private final VehicleService vehicleService;

    /**
     * Página para criar nova viagem
     */
    @GetMapping("/trips/create")
    public String createTripPage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("email");
        String token = (String) session.getAttribute("token");
        Long userId = (Long) session.getAttribute("userId");

        if (email == null || token == null) {
            return "redirect:/login";
        }

        // Carregar veículos do condutor
        List<VehicleDTO> vehicles = vehicleService.getDriverVehicles(userId, token);
        model.addAttribute("vehicles", vehicles);
        model.addAttribute("userEmail", email);
        return "trips/create-trip";
    }

    /**
     * Endpoint AJAX para calcular rota e estimativas de combustível/portagens
     */
    @GetMapping("/trips/api/calculate-route")
    @ResponseBody
    public Map<String, Object> calculateRoute(
            @RequestParam String origin,
            @RequestParam String destination) {
        return tripService.calculateRoute(origin, destination);
    }

    /**
     * Processa criação de viagem
     */
    @PostMapping("/trips/create")
    public String createTrip(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam(required = false) Double originLat,
            @RequestParam(required = false) Double originLng,
            @RequestParam(required = false) Double destLat,
            @RequestParam(required = false) Double destLng,
            @RequestParam String departureDateTime,
            @RequestParam Integer availableSeats,
            @RequestParam(required = false) Double totalCost,
            @RequestParam(required = false) Double estimatedFuelCost,
            @RequestParam(required = false) Double estimatedTolls,
            @RequestParam(required = false) Double distance,
            @RequestParam(required = false) Double co2SavedKg,
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) String vehicleInfo,
            @RequestParam(required = false) String description,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute("email");
        String token = (String) session.getAttribute("token");
        Long userId = (Long) session.getAttribute("userId");
        String fullName = (String) session.getAttribute("fullName");

        if (email == null || token == null) {
            return "redirect:/login";
        }

        try {
            BigDecimal tot = totalCost != null ? BigDecimal.valueOf(totalCost) : BigDecimal.valueOf(15.0);
            BigDecimal fuel = estimatedFuelCost != null ? BigDecimal.valueOf(estimatedFuelCost) : BigDecimal.ZERO;
            BigDecimal tolls = estimatedTolls != null ? BigDecimal.valueOf(estimatedTolls) : BigDecimal.ZERO;
            int seats = availableSeats != null ? availableSeats : 3;
            BigDecimal costPerPass = tot.divide(BigDecimal.valueOf(seats), 2, java.math.RoundingMode.HALF_UP);

            TripDTO trip = TripDTO.builder()
                    .driverId(userId != null ? userId : 1L)
                    .driverName(fullName != null ? fullName : email)
                    .vehicleId(vehicleId)
                    .vehicleInfo(vehicleInfo != null && !vehicleInfo.isBlank() ? vehicleInfo : "Viatura Particular")
                    .departureLocation(origin)
                    .destinationLocation(destination)
                    .originLatitude(originLat)
                    .originLongitude(originLng)
                    .destinationLatitude(destLat)
                    .destinationLongitude(destLng)
                    .departureDateTime(departureDateTime)
                    .availableSeats(seats)
                    .occupiedSeats(0)
                    .totalCost(tot)
                    .costPerPassenger(costPerPass)
                    .estimatedFuelCost(fuel)
                    .estimatedTolls(tolls)
                    .distance(distance != null ? distance : 100.0)
                    .co2SavedKg(co2SavedKg != null ? co2SavedKg : 12.5)
                    .description(description)
                    .status("OPEN")
                    .build();

            tripService.createTrip(trip, token);
            redirectAttributes.addFlashAttribute("success", "Viagem publicada com sucesso! Os passageiros já podem pesquisar.");
            return "redirect:/trips/my-trips";

        } catch (Exception e) {
            log.error("Erro ao criar viagem: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Erro ao criar viagem: " + e.getMessage());
            return "redirect:/trips/create";
        }
    }

    /**
     * Pesquisa de viagens
     */
    @GetMapping("/trips/search")
    public String searchTripsPage(
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) String date,
            HttpSession session,
            Model model) {

        String token = (String) session.getAttribute("token");

        try {
            List<TripDTO> trips;
            boolean temPesquisa = (origin != null && !origin.isBlank()) || (destination != null && !destination.isBlank());
            if (temPesquisa) {
                trips = tripService.searchTrips(origin, destination, token);
            } else {
                trips = tripService.getAllAvailableTrips(token);
            }

            model.addAttribute("trips", trips);
            model.addAttribute("resultados", true);
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
     * Minhas Viagens (criadas por mim enquanto condutor)
     */
    @GetMapping("/trips/my-trips")
    public String myTripsPage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("email");
        String token = (String) session.getAttribute("token");
        Long userId = (Long) session.getAttribute("userId");

        if (email == null || token == null) {
            return "redirect:/login";
        }

        try {
            var myTrips = tripService.getMyTrips(userId, token);
            model.addAttribute("trips", myTrips);
            return "trips/my-trips";
        } catch (Exception e) {
            log.error("Erro ao carregar viagens: {}", e.getMessage());
            model.addAttribute("error", "Erro ao carregar viagens");
            return "trips/my-trips";
        }
    }

    /**
     * Minhas Reservas (enquanto passageiro)
     */
    @GetMapping("/trips/my-reservations")
    public String myReservationsPage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("email");
        String token = (String) session.getAttribute("token");
        Long userId = (Long) session.getAttribute("userId");

        if (email == null || token == null) {
            return "redirect:/login";
        }

        try {
            var reservations = tripService.getMyReservations(userId, token);
            model.addAttribute("reservations", reservations);
            return "trips/my-reservations";
        } catch (Exception e) {
            log.error("Erro ao carregar reservas: {}", e.getMessage());
            model.addAttribute("error", "Erro ao carregar reservas");
            return "trips/my-reservations";
        }
    }

    /**
     * Pedidos de Boleia recebidos pelo condutor
     */
    @GetMapping("/trips/driver-requests")
    public String driverRequestsPage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("email");
        String token = (String) session.getAttribute("token");
        Long userId = (Long) session.getAttribute("userId");

        if (email == null || token == null) {
            return "redirect:/login";
        }

        try {
            List<ReservationDTO> requests = tripService.getDriverReservations(userId, token);
            model.addAttribute("requests", requests);
            return "trips/driver-requests";
        } catch (Exception e) {
            log.error("Erro ao carregar pedidos para condutor: {}", e.getMessage());
            model.addAttribute("error", "Erro ao carregar pedidos de passageiros");
            return "trips/driver-requests";
        }
    }

    /**
     * Passageiro reserva lugar
     */
    @PostMapping("/trips/reserve")
    public String makeReservation(
            @RequestParam Long tripId,
            @RequestParam(defaultValue = "1") Integer seats,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute("email");
        String token = (String) session.getAttribute("token");
        Long userId = (Long) session.getAttribute("userId");
        String fullName = (String) session.getAttribute("fullName");

        if (email == null || token == null) {
            return "redirect:/login";
        }

        try {
            tripService.makeReservation(tripId, userId, fullName, email, seats, token);
            redirectAttributes.addFlashAttribute("success", "Pedido de reserva enviado ao condutor! Receberá confirmação.");
            return "redirect:/trips/my-reservations";

        } catch (Exception e) {
            log.error("Erro ao fazer reserva: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Erro ao fazer reserva: " + e.getMessage());
            return "redirect:/trips/search";
        }
    }

    /**
     * Condutor confirma reserva
     */
    @PostMapping("/trips/reservations/{id}/confirm")
    public String confirmReservation(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        String token = (String) session.getAttribute("token");
        if (token == null) return "redirect:/login";

        try {
            tripService.confirmReservation(id, token);
            redirectAttributes.addFlashAttribute("success", "Reserva confirmada! O lugar foi atribuído ao passageiro.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao confirmar reserva: " + e.getMessage());
        }
        return "redirect:/trips/driver-requests";
    }

    /**
     * Condutor rejeita reserva
     */
    @PostMapping("/trips/reservations/{id}/reject")
    public String rejectReservation(
            @PathVariable Long id, 
            @RequestParam(required = false, defaultValue = "Lotação completa") String reason,
            HttpSession session, 
            RedirectAttributes redirectAttributes) {
        String token = (String) session.getAttribute("token");
        if (token == null) return "redirect:/login";

        try {
            tripService.rejectReservation(id, reason, token);
            redirectAttributes.addFlashAttribute("success", "Reserva rejeitada com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao rejeitar reserva: " + e.getMessage());
        }
        return "redirect:/trips/driver-requests";
    }

    /**
     * Cancelar reserva
     */
    @PostMapping("/trips/reservations/{id}/cancel")
    public String cancelReservation(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        String token = (String) session.getAttribute("token");
        if (token == null) return "redirect:/login";

        try {
            tripService.cancelReservation(id, token);
            redirectAttributes.addFlashAttribute("success", "Reserva cancelada com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao cancelar reserva: " + e.getMessage());
        }
        return "redirect:/trips/my-reservations";
    }

    /**
     * Iniciar ou concluir viagem
     */
    @PostMapping("/trips/{id}/status")
    public String updateTripStatus(
            @PathVariable Long id,
            @RequestParam String status,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        String token = (String) session.getAttribute("token");
        if (token == null) return "redirect:/login";

        try {
            tripService.updateTripStatus(id, status, token);
            redirectAttributes.addFlashAttribute("success", "Estado da viagem atualizado para " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao atualizar estado: " + e.getMessage());
        }
        return "redirect:/trips/my-trips";
    }
}

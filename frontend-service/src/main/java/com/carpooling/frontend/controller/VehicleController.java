package com.carpooling.frontend.controller;

import com.carpooling.frontend.dto.VehicleDTO;
import com.carpooling.frontend.service.VehicleService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/vehicles")
@Slf4j
@AllArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    public String listVehicles(HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        Long userId = (Long) session.getAttribute("userId");

        if (token == null) return "redirect:/login";

        List<VehicleDTO> vehicles = vehicleService.getDriverVehicles(userId, token);
        model.addAttribute("vehicles", vehicles);
        return "vehicles/vehicles";
    }

    @PostMapping("/add")
    public String addVehicle(
            @RequestParam String brand,
            @RequestParam String model,
            @RequestParam String licensePlate,
            @RequestParam Integer year,
            @RequestParam String color,
            @RequestParam(defaultValue = "4") Integer seatCapacity,
            @RequestParam(defaultValue = "GASOLINE") String fuelType,
            @RequestParam(defaultValue = "5.5") Double fuelConsumptionLPer100Km,
            @RequestParam(defaultValue = "115.0") Double co2EmissionsGPerKm,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String token = (String) session.getAttribute("token");
        Long userId = (Long) session.getAttribute("userId");

        if (token == null) return "redirect:/login";

        try {
            VehicleDTO vehicle = VehicleDTO.builder()
                    .brand(brand)
                    .model(model)
                    .licensePlate(licensePlate.toUpperCase().trim())
                    .year(year)
                    .color(color)
                    .seatCapacity(seatCapacity)
                    .fuelType(fuelType)
                    .fuelConsumptionLPer100Km(fuelConsumptionLPer100Km)
                    .co2EmissionsGPerKm(co2EmissionsGPerKm)
                    .ownerId(userId != null ? userId : 1L)
                    .isDefault(false)
                    .build();

            vehicleService.addVehicle(vehicle, token);
            redirectAttributes.addFlashAttribute("success", "Veículo adicionado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao adicionar veículo: " + e.getMessage());
        }

        return "redirect:/vehicles";
    }

    @PostMapping("/{id}/delete")
    public String deleteVehicle(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        String token = (String) session.getAttribute("token");
        if (token == null) return "redirect:/login";

        try {
            vehicleService.deleteVehicle(id, token);
            redirectAttributes.addFlashAttribute("success", "Veículo removido com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao remover veículo: " + e.getMessage());
        }

        return "redirect:/vehicles";
    }
}

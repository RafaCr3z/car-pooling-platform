package com.carpooling.frontend.service;

import com.carpooling.frontend.dto.VehicleDTO;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class VehicleService {

    private final WebClient webClient;

    public List<VehicleDTO> getDriverVehicles(Long driverId, String token) {
        try {
            if (driverId == null) return Collections.emptyList();
            String response = webClient.get()
                    .uri("/api/vehicles/driver/" + driverId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            List<VehicleDTO> vehicles = new ArrayList<>();
            JsonArray array = JsonParser.parseString(response).getAsJsonArray();
            for (var el : array) {
                JsonObject o = el.getAsJsonObject();
                vehicles.add(VehicleDTO.builder()
                        .id(optLong(o, "id"))
                        .licensePlate(optString(o, "licensePlate"))
                        .brand(optString(o, "brand"))
                        .model(optString(o, "model"))
                        .year(optInt(o, "year"))
                        .color(optString(o, "color"))
                        .seatCapacity(optInt(o, "seatCapacity"))
                        .fuelType(optString(o, "fuelType"))
                        .fuelConsumptionLPer100Km(optDouble(o, "fuelConsumptionLPer100Km"))
                        .co2EmissionsGPerKm(optDouble(o, "co2EmissionsGPerKm"))
                        .ownerId(optLong(o, "ownerId"))
                        .isDefault(optBoolean(o, "isDefault"))
                        .build());
            }
            return vehicles;
        } catch (Exception e) {
            log.warn("Erro ao buscar veículos do condutor: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public void addVehicle(VehicleDTO vehicle, String token) {
        try {
            webClient.post()
                    .uri("/api/vehicles")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(vehicle)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            log.info("Veículo adicionado: {} {}", vehicle.getBrand(), vehicle.getModel());
        } catch (Exception e) {
            log.error("Erro ao adicionar veículo: {}", e.getMessage());
            throw new RuntimeException("Erro ao adicionar veículo: " + e.getMessage(), e);
        }
    }

    public void deleteVehicle(Long vehicleId, String token) {
        try {
            webClient.delete()
                    .uri("/api/vehicles/" + vehicleId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            log.error("Erro ao remover veículo: {}", e.getMessage());
        }
    }

    private String optString(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : null;
    }
    private Long optLong(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsLong() : null;
    }
    private Integer optInt(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsInt() : 0;
    }
    private Double optDouble(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsDouble() : 0.0;
    }
    private Boolean optBoolean(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() && o.get(key).getAsBoolean();
    }
}

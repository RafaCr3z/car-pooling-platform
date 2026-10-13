package com.carpooling.frontend.service;

import com.carpooling.frontend.dto.ReservationDTO;
import com.carpooling.frontend.dto.TripDTO;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class TripService {

    private final WebClient webClient;
    private final Gson gson = new Gson();

    /**
     * Criar nova viagem completa
     */
    public void createTrip(TripDTO trip, String token) {
        try {
            webClient.post()
                    .uri("/api/trips")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(trip)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Viagem criada com sucesso: {} -> {}", trip.getDepartureLocation(), trip.getDestinationLocation());

        } catch (Exception e) {
            log.error("Erro ao criar viagem: {}", e.getMessage());
            throw new RuntimeException("Erro ao criar viagem: " + e.getMessage(), e);
        }
    }

    /**
     * Pesquisar viagens por origem/destino
     */
    public List<TripDTO> searchTrips(String origin, String destination, String token) {
        try {
            String uri;
            if (origin != null && !origin.isBlank() && destination != null && !destination.isBlank()) {
                uri = "/api/trips/search?departure=" + origin.trim() + "&destination=" + destination.trim();
            } else {
                uri = "/api/trips/available";
            }

            var request = webClient.get().uri(uri);
            if (token != null && !token.isBlank()) {
                request = request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            }

            String response = request.retrieve().bodyToMono(String.class).block();
            return parseTrips(response);

        } catch (Exception e) {
            log.warn("Erro na pesquisa de viagens (retornando lista vazia): {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Obter todas as viagens disponíveis
     */
    public List<TripDTO> getAllAvailableTrips(String token) {
        try {
            var request = webClient.get().uri("/api/trips/available");
            if (token != null && !token.isBlank()) {
                request = request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            }
            String response = request.retrieve().bodyToMono(String.class).block();
            return parseTrips(response);
        } catch (Exception e) {
            log.warn("Erro ao buscar viagens disponíveis: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Obter viagens criadas pelo condutor
     */
    public List<TripDTO> getMyTrips(Long driverId, String token) {
        try {
            String uri = driverId != null ? "/api/trips/driver/" + driverId : "/api/trips";
            String response = webClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseTrips(response);

        } catch (Exception e) {
            log.error("Erro ao carregar viagens do condutor: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Obter reservas feitas pelo passageiro
     */
    public List<ReservationDTO> getMyReservations(Long passengerId, String token) {
        try {
            if (passengerId == null) return Collections.emptyList();
            String response = webClient.get()
                    .uri("/api/trips/reservations/passenger/" + passengerId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseReservations(response);

        } catch (Exception e) {
            log.error("Erro ao carregar reservas do passageiro: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Obter reservas recebidas pelo condutor para gerir
     */
    public List<ReservationDTO> getDriverReservations(Long driverId, String token) {
        try {
            if (driverId == null) return Collections.emptyList();
            String response = webClient.get()
                    .uri("/api/trips/reservations/driver/" + driverId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseReservations(response);

        } catch (Exception e) {
            log.error("Erro ao carregar pedidos para condutor: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Efetuar reserva de lugar(es)
     */
    public void makeReservation(Long tripId, Long passengerId, String passengerName, String passengerEmail, Integer seats, String token) {
        try {
            Map<String, Object> body = Map.of(
                    "tripId", tripId,
                    "passengerId", passengerId != null ? passengerId : 1L,
                    "passengerName", passengerName != null ? passengerName : "Passageiro",
                    "passengerEmail", passengerEmail != null ? passengerEmail : "passageiro@carpooling.pt",
                    "numberOfSeats", seats != null ? seats : 1
            );

            webClient.post()
                    .uri("/api/trips/reservations")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Reserva solicitada com sucesso: tripId={} passageiro={}", tripId, passengerName);

        } catch (Exception e) {
            log.error("Erro ao fazer reserva: {}", e.getMessage());
            throw new RuntimeException("Erro ao fazer reserva: " + e.getMessage(), e);
        }
    }

    /**
     * Condutor confirma reserva de passageiro
     */
    public void confirmReservation(Long reservationId, String token) {
        webClient.post()
                .uri("/api/trips/reservations/" + reservationId + "/confirm")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    /**
     * Condutor rejeita reserva de passageiro
     */
    public void rejectReservation(Long reservationId, String reason, String token) {
        webClient.post()
                .uri("/api/trips/reservations/" + reservationId + "/reject")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("reason", reason != null ? reason : "Lotação completa"))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    /**
     * Passageiro cancela a sua reserva
     */
    public void cancelReservation(Long reservationId, String token) {
        webClient.post()
                .uri("/api/trips/reservations/" + reservationId + "/cancel")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    /**
     * Atualizar estado da viagem (STARTED, COMPLETED, CANCELLED)
     */
    public void updateTripStatus(Long tripId, String status, String token) {
        webClient.put()
                .uri("/api/trips/" + tripId + "/status?status=" + status)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    /**
     * Calcular rota e custos via Location Service
     */
    public Map<String, Object> calculateRoute(String origin, String destination) {
        try {
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/locations/route-cities")
                            .queryParam("origin", origin)
                            .queryParam("destination", destination)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return gson.fromJson(response, Map.class);
        } catch (Exception e) {
            log.warn("Erro ao calcular rota via location-service: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private List<TripDTO> parseTrips(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        List<TripDTO> list = new ArrayList<>();
        JsonArray array = JsonParser.parseString(json).getAsJsonArray();
        for (var el : array) {
            JsonObject o = el.getAsJsonObject();
            list.add(TripDTO.builder()
                    .id(optLong(o, "id"))
                    .driverId(optLong(o, "driverId"))
                    .driverName(optString(o, "driverName"))
                    .vehicleId(optLong(o, "vehicleId"))
                    .vehicleInfo(optString(o, "vehicleInfo"))
                    .departureLocation(optString(o, "departureLocation"))
                    .destinationLocation(optString(o, "destinationLocation"))
                    .originLatitude(optDouble(o, "originLatitude"))
                    .originLongitude(optDouble(o, "originLongitude"))
                    .destinationLatitude(optDouble(o, "destinationLatitude"))
                    .destinationLongitude(optDouble(o, "destinationLongitude"))
                    .departureDateTime(optString(o, "departureDateTime"))
                    .availableSeats(optInt(o, "availableSeats"))
                    .occupiedSeats(optInt(o, "occupiedSeats"))
                    .totalCost(optBigDecimal(o, "totalCost"))
                    .costPerPassenger(optBigDecimal(o, "costPerPassenger"))
                    .estimatedFuelCost(optBigDecimal(o, "estimatedFuelCost"))
                    .estimatedTolls(optBigDecimal(o, "estimatedTolls"))
                    .distance(optDouble(o, "distance"))
                    .co2SavedKg(optDouble(o, "co2SavedKg"))
                    .description(optString(o, "description"))
                    .status(optString(o, "status"))
                    .build());
        }
        return list;
    }

    private List<ReservationDTO> parseReservations(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        List<ReservationDTO> list = new ArrayList<>();
        JsonArray array = JsonParser.parseString(json).getAsJsonArray();
        for (var el : array) {
            JsonObject o = el.getAsJsonObject();
            list.add(ReservationDTO.builder()
                    .id(optLong(o, "id"))
                    .tripId(optLong(o, "tripId"))
                    .passengerId(optLong(o, "passengerId"))
                    .passengerName(optString(o, "passengerName"))
                    .passengerEmail(optString(o, "passengerEmail"))
                    .driverId(optLong(o, "driverId"))
                    .driverName(optString(o, "driverName"))
                    .departureLocation(optString(o, "departureLocation"))
                    .destinationLocation(optString(o, "destinationLocation"))
                    .departureDateTime(optString(o, "departureDateTime"))
                    .numberOfSeats(optInt(o, "numberOfSeats"))
                    .totalPrice(optBigDecimal(o, "totalPrice"))
                    .status(optString(o, "status"))
                    .rejectionReason(optString(o, "rejectionReason"))
                    .createdAt(optString(o, "createdAt"))
                    .build());
        }
        return list;
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
    private BigDecimal optBigDecimal(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? BigDecimal.valueOf(o.get(key).getAsDouble()) : BigDecimal.ZERO;
    }
}

package com.carpooling.frontend.service;

import com.carpooling.frontend.dto.ReservationDTO;
import com.carpooling.frontend.dto.TripDTO;
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
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class TripService {

    private final WebClient webClient;

    /**
     * Criar nova viagem
     */
    public void createTrip(String origin, String destination, String departureDateTime,
                          Integer availableSeats, Double totalCost, String token) {
        try {
            String requestBody = String.format(
                    "{\"departureLocation\":\"%s\",\"destinationLocation\":\"%s\"," +
                    "\"departureDateTime\":\"%s\",\"availableSeats\":%d,\"totalCost\":%.2f}",
                    origin, destination, departureDateTime, availableSeats, totalCost
            );

            webClient.post()
                    .uri("http://localhost:8080/api/trips")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Viagem criada com sucesso: {} -> {}", origin, destination);

        } catch (Exception e) {
            log.error("Erro ao criar viagem: {}", e.getMessage());
            throw new RuntimeException("Erro ao criar viagem", e);
        }
    }

    /**
     * Pesquisar viagens
     */
    public List<TripDTO> searchTrips(String origin, String destination, String token) {
        try {
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("http://localhost:8080/api/trips/search")
                            .queryParam("origin", origin)
                            .queryParam("destination", destination)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            List<TripDTO> trips = new ArrayList<>();
            JsonArray jsonArray = JsonParser.parseString(response).getAsJsonArray();

            for (var element : jsonArray) {
                JsonObject trip = element.getAsJsonObject();
                trips.add(new TripDTO(
                        trip.get("id").getAsLong(),
                        trip.get("departureLocation").getAsString(),
                        trip.get("destinationLocation").getAsString(),
                        trip.get("departureDateTime").getAsString(),
                        trip.get("availableSeats").getAsInt(),
                        trip.get("occupiedSeats").getAsInt(),
                        trip.get("costPerPassenger").getAsDouble()
                ));
            }

            log.info("Encontradas {} viagens", trips.size());
            return trips;

        } catch (Exception e) {
            log.error("Erro na pesquisa: {}", e.getMessage());
            throw new RuntimeException("Erro na pesquisa", e);
        }
    }

    /**
     * Obter minhas viagens
     */
    public List<TripDTO> getMyTrips(String token) {
        try {
            String response = webClient.get()
                    .uri("http://localhost:8080/api/trips")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            List<TripDTO> trips = new ArrayList<>();
            JsonArray jsonArray = JsonParser.parseString(response).getAsJsonArray();

            for (var element : jsonArray) {
                JsonObject trip = element.getAsJsonObject();
                trips.add(new TripDTO(
                        trip.get("id").getAsLong(),
                        trip.get("departureLocation").getAsString(),
                        trip.get("destinationLocation").getAsString(),
                        trip.get("departureDateTime").getAsString(),
                        trip.get("availableSeats").getAsInt(),
                        trip.get("occupiedSeats").getAsInt(),
                        trip.get("costPerPassenger").getAsDouble()
                ));
            }

            return trips;

        } catch (Exception e) {
            log.error("Erro ao carregar viagens: {}", e.getMessage());
            throw new RuntimeException("Erro ao carregar viagens", e);
        }
    }

    /**
     * Obter minhas reservas
     */
    public List<ReservationDTO> getMyReservations(String token) {
        try {
            String response = webClient.get()
                    .uri("http://localhost:8080/api/trips/reservations")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            List<ReservationDTO> reservations = new ArrayList<>();
            JsonArray jsonArray = JsonParser.parseString(response).getAsJsonArray();

            for (var element : jsonArray) {
                JsonObject reservation = element.getAsJsonObject();
                reservations.add(new ReservationDTO(
                        reservation.get("id").getAsLong(),
                        reservation.get("tripId").getAsLong(),
                        reservation.get("numberOfSeats").getAsInt(),
                        reservation.get("totalPrice").getAsDouble(),
                        reservation.get("status").getAsString()
                ));
            }

            return reservations;

        } catch (Exception e) {
            log.error("Erro ao carregar reservas: {}", e.getMessage());
            throw new RuntimeException("Erro ao carregar reservas", e);
        }
    }

    /**
     * Fazer reserva de viagem
     */
    public void makeReservation(Long tripId, Integer seats, String token) {
        try {
            String requestBody = String.format(
                    "{\"tripId\":%d,\"numberOfSeats\":%d}",
                    tripId, seats
            );

            webClient.post()
                    .uri("http://localhost:8080/api/trips/reservations")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Reserva realizada com sucesso para viagem: {}", tripId);

        } catch (Exception e) {
            log.error("Erro ao fazer reserva: {}", e.getMessage());
            throw new RuntimeException("Erro ao fazer reserva", e);
        }
    }

}

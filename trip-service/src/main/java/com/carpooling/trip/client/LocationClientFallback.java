package com.carpooling.trip.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class LocationClientFallback implements LocationClient {

    @Override
    public Map<String, Object> getRouteBetweenCities(String origin, String destination) {
        log.warn("LocationClient fallback acionado para rota entre {} e {}", origin, destination);
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("origin", origin);
        fallback.put("destination", destination);
        fallback.put("distanceKm", 220.0);
        fallback.put("durationMinutes", 145L);
        fallback.put("estimatedFuelCost", BigDecimal.valueOf(20.57));
        fallback.put("estimatedTolls", BigDecimal.valueOf(14.30));
        fallback.put("totalEstimatedCost", BigDecimal.valueOf(34.87));
        fallback.put("waypoints", List.of(List.of(39.8222, -7.4909), List.of(38.7223, -9.1393)));
        return fallback;
    }
}

package com.carpooling.location.service;

import com.carpooling.location.dto.LocationDTO;
import com.carpooling.location.dto.RouteDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    public record CityLocation(String name, double lat, double lon, String district) {}

    // Coordenadas geográficas das principais cidades e vilas de Portugal
    private static final Map<String, CityLocation> PORTUGAL_CITIES = new LinkedHashMap<>();

    static {
        registerCity("Castelo Branco", 39.8222, -7.4909, "Castelo Branco");
        registerCity("Lisboa", 38.7223, -9.1393, "Lisboa");
        registerCity("Porto", 41.1579, -8.6291, "Porto");
        registerCity("Coimbra", 40.2033, -8.4103, "Coimbra");
        registerCity("Covilhã", 40.2828, -7.5033, "Castelo Branco");
        registerCity("Guarda", 40.5373, -7.2678, "Guarda");
        registerCity("Fundão", 40.1408, -7.5019, "Castelo Branco");
        registerCity("Évora", 38.5714, -7.9135, "Évora");
        registerCity("Faro", 37.0194, -7.9304, "Faro");
        registerCity("Braga", 41.5454, -8.4265, "Braga");
        registerCity("Aveiro", 40.6405, -8.6538, "Aveiro");
        registerCity("Leiria", 39.7436, -8.8071, "Leiria");
        registerCity("Santarém", 39.2369, -8.6855, "Santarém");
        registerCity("Viseu", 40.6566, -7.9125, "Viseu");
        registerCity("Setúbal", 38.5244, -8.8882, "Setúbal");
        registerCity("Bragança", 41.8058, -6.7572, "Bragança");
        registerCity("Viana do Castelo", 41.6918, -8.8344, "Viana do Castelo");
        registerCity("Portalegre", 39.2938, -7.4312, "Portalegre");
        registerCity("Beja", 38.0151, -7.8632, "Beja");
        registerCity("Vila Real", 41.3006, -7.7441, "Vila Real");
    }

    private static void registerCity(String name, double lat, double lon, String district) {
        PORTUGAL_CITIES.put(name.toLowerCase(), new CityLocation(name, lat, lon, district));
    }

    public List<CityLocation> getAllCities() {
        return new ArrayList<>(PORTUGAL_CITIES.values());
    }

    public Optional<CityLocation> findCity(String name) {
        if (name == null || name.isBlank()) return Optional.empty();
        String clean = name.trim().toLowerCase();
        for (Map.Entry<String, CityLocation> entry : PORTUGAL_CITIES.entrySet()) {
            if (entry.getKey().contains(clean) || clean.contains(entry.getKey())) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }

    /**
     * Calcula a distância geodésica entre dois pontos (fórmula de Haversine)
     */
    public Double calculateDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return 0.0;
        }
        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1.doubleValue())) * Math.cos(Math.toRadians(lat2.doubleValue()))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double haversineDistance = 6371.0 * c; // km em linha reta

        // Fator de rota rodoviária (curvatura média das estradas em PT: ~1.22x)
        double roadDistance = haversineDistance * 1.22;
        return BigDecimal.valueOf(roadDistance).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Estima o tempo de viagem (velocidade média de 90 km/h em autoestrada/nacional)
     */
    public Long estimateTravelTime(Double distanceKm) {
        if (distanceKm == null || distanceKm <= 0) return 0L;
        double averageSpeed = 85.0; // km/h
        return Math.round((distanceKm / averageSpeed) * 60); // minutos
    }

    /**
     * Estima custos de portagens e combustível para uma distância
     */
    public Map<String, Object> calculateRouteBetweenCities(String originCity, String destinationCity) {
        CityLocation origin = findCity(originCity).orElse(new CityLocation(originCity, 39.8222, -7.4909, "Portugal"));
        CityLocation dest = findCity(destinationCity).orElse(new CityLocation(destinationCity, 38.7223, -9.1393, "Portugal"));

        Double distance = calculateDistance(
                BigDecimal.valueOf(origin.lat()),
                BigDecimal.valueOf(origin.lon()),
                BigDecimal.valueOf(dest.lat()),
                BigDecimal.valueOf(dest.lon())
        );

        Long durationMinutes = estimateTravelTime(distance);

        // Estimativa de combustível: 5.5 L/100km a 1.70 €/L
        BigDecimal fuelCost = BigDecimal.valueOf((distance / 100.0) * 5.5 * 1.70)
                .setScale(2, RoundingMode.HALF_UP);

        // Estimativa de portagens: cerca de 0.07 €/km se for rota longa
        BigDecimal tolls = distance > 80.0
                ? BigDecimal.valueOf(distance * 0.065).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal totalCost = fuelCost.add(tolls);

        // Waypoints simulados para o Leaflet desenhar a rota entre as duas cidades
        List<List<Double>> waypoints = new ArrayList<>();
        waypoints.add(List.of(origin.lat(), origin.lon()));
        
        // Ponto intermédio suave
        double midLat = (origin.lat() + dest.lat()) / 2.0;
        double midLon = (origin.lon() + dest.lon()) / 2.0;
        waypoints.add(List.of(midLat, midLon));
        
        waypoints.add(List.of(dest.lat(), dest.lon()));

        Map<String, Object> result = new HashMap<>();
        result.put("origin", origin.name());
        result.put("originLat", origin.lat());
        result.put("originLon", origin.lon());
        result.put("destination", dest.name());
        result.put("destinationLat", dest.lat());
        result.put("destinationLon", dest.lon());
        result.put("distanceKm", distance);
        result.put("durationMinutes", durationMinutes);
        result.put("estimatedFuelCost", fuelCost);
        result.put("estimatedTolls", tolls);
        result.put("totalEstimatedCost", totalCost);
        result.put("waypoints", waypoints);

        return result;
    }

    public RouteDTO calculateRoute(RouteDTO routeRequest) {
        Double distance = calculateDistance(
                routeRequest.getOriginLatitude(),
                routeRequest.getOriginLongitude(),
                routeRequest.getDestLatitude(),
                routeRequest.getDestLongitude()
        );
        Long estimatedTime = estimateTravelTime(distance);
        routeRequest.setDistance(distance);
        routeRequest.setEstimatedTime(estimatedTime);
        return routeRequest;
    }

    public boolean isValidLocation(LocationDTO location) {
        if (location == null || location.getAddress() == null || location.getAddress().isBlank()) {
            return false;
        }
        BigDecimal lat = location.getLatitude();
        BigDecimal lon = location.getLongitude();
        if (lat == null || lon == null) {
            return false;
        }
        return lat.doubleValue() >= -90 && lat.doubleValue() <= 90 && lon.doubleValue() >= -180 && lon.doubleValue() <= 180;
    }
}

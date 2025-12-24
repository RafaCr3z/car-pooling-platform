package com.carpooling.location.service;

import com.carpooling.location.dto.LocationDTO;
import com.carpooling.location.dto.RouteDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class LocationService {
    
    /**
     * Calcula a distância entre dois pontos usando a fórmula Haversine
     * @param lat1 latitude do ponto 1
     * @param lon1 longitude do ponto 1
     * @param lat2 latitude do ponto 2
     * @param lon2 longitude do ponto 2
     * @return distância em quilômetros
     */
    public Double calculateDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        double latDistance = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double lonDistance = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1.doubleValue())) * Math.cos(Math.toRadians(lat2.doubleValue()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double radius = 6371; // Radius of the earth in km
        return radius * c;
    }
    
    /**
     * Estima o tempo de viagem (simulado)
     * Assume uma velocidade média de 50 km/h
     * @param distanceKm distância em quilômetros
     * @return tempo estimado em minutos
     */
    public Long estimateTravelTime(Double distanceKm) {
        double averageSpeed = 50.0; // km/h
        return Math.round((distanceKm / averageSpeed) * 60); // convert to minutes
    }
    
    /**
     * Calcula uma rota entre dois pontos
     */
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
    
    /**
     * Valida se uma localização é válida
     */
    public boolean isValidLocation(LocationDTO location) {
        if (location == null || location.getAddress() == null || location.getAddress().isBlank()) {
            return false;
        }
        
        BigDecimal lat = location.getLatitude();
        BigDecimal lon = location.getLongitude();
        
        if (lat == null || lon == null) {
            return false;
        }
        
        // Validar ranges de latitude e longitude
        if (lat.doubleValue() < -90 || lat.doubleValue() > 90) {
            return false;
        }
        
        if (lon.doubleValue() < -180 || lon.doubleValue() > 180) {
            return false;
        }
        
        return true;
    }
}

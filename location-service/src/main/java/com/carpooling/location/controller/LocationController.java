package com.carpooling.location.controller;

import com.carpooling.location.dto.LocationDTO;
import com.carpooling.location.dto.RouteDTO;
import com.carpooling.location.service.LocationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/locations")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class LocationController {
    
    private final LocationService locationService;
    
    @GetMapping("/cities")
    public ResponseEntity<List<LocationService.CityLocation>> getAllCities() {
        return ResponseEntity.ok(locationService.getAllCities());
    }

    @GetMapping("/geocode")
    public ResponseEntity<LocationService.CityLocation> geocodeCity(@RequestParam String city) {
        return locationService.findCity(city)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/route-cities")
    public ResponseEntity<Map<String, Object>> getRouteBetweenCities(
            @RequestParam String origin,
            @RequestParam String destination) {
        return ResponseEntity.ok(locationService.calculateRouteBetweenCities(origin, destination));
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateLocation(@Valid @RequestBody LocationDTO location) {
        boolean isValid = locationService.isValidLocation(location);
        return ResponseEntity.ok(Map.of(
                "address", location.getAddress(),
                "isValid", isValid
        ));
    }
    
    @PostMapping("/distance")
    public ResponseEntity<Map<String, Object>> calculateDistance(@RequestBody Map<String, BigDecimal> request) {
        BigDecimal lat1 = request.get("lat1");
        BigDecimal lon1 = request.get("lon1");
        BigDecimal lat2 = request.get("lat2");
        BigDecimal lon2 = request.get("lon2");
        
        Double distance = locationService.calculateDistance(lat1, lon1, lat2, lon2);
        
        return ResponseEntity.ok(Map.of(
                "distance", distance,
                "unit", "km"
        ));
    }
    
    @PostMapping("/route")
    public ResponseEntity<RouteDTO> calculateRoute(@Valid @RequestBody RouteDTO routeRequest) {
        RouteDTO route = locationService.calculateRoute(routeRequest);
        return ResponseEntity.ok(route);
    }
    
    @PostMapping("/estimate-time")
    public ResponseEntity<Map<String, Object>> estimateTravelTime(@RequestBody Map<String, Double> request) {
        Double distanceKm = request.get("distanceKm");
        Long estimatedMinutes = locationService.estimateTravelTime(distanceKm);
        
        return ResponseEntity.ok(Map.of(
                "distanceKm", distanceKm,
                "estimatedMinutes", estimatedMinutes,
                "estimatedHours", estimatedMinutes / 60.0
        ));
    }
}

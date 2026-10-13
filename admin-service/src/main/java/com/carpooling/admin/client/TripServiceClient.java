package com.carpooling.admin.client;

import com.carpooling.admin.client.fallback.TripClientFallback;
import com.carpooling.admin.dto.TripResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "trip-service", fallback = TripClientFallback.class)
public interface TripServiceClient {

    @GetMapping("/api/trips")
    List<TripResponseDTO> getAllTrips();

    @PutMapping("/api/trips/{id}/status")
    TripResponseDTO updateTripStatus(@PathVariable("id") Long id, @RequestParam("status") String status);
}

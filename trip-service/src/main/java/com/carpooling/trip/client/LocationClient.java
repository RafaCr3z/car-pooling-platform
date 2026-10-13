package com.carpooling.trip.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "location-service", fallback = LocationClientFallback.class)
public interface LocationClient {

    @GetMapping("/api/locations/route-cities")
    Map<String, Object> getRouteBetweenCities(
            @RequestParam("origin") String origin,
            @RequestParam("destination") String destination
    );
}

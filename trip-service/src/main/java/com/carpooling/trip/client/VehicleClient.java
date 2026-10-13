package com.carpooling.trip.client;

import com.carpooling.common.dto.VehicleSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "vehicle-service", fallback = VehicleClientFallback.class)
public interface VehicleClient {

    @GetMapping("/api/vehicles/{id}")
    VehicleSummaryDTO getVehicleById(@PathVariable("id") Long id);

    @GetMapping("/api/vehicles/driver/{driverId}")
    List<VehicleSummaryDTO> getVehiclesByDriver(@PathVariable("driverId") Long driverId);
}

package com.carpooling.vehicle.controller;

import com.carpooling.vehicle.dto.CreateVehicleRequest;
import com.carpooling.vehicle.dto.VehicleDTO;
import com.carpooling.vehicle.service.VehicleService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicles")
@AllArgsConstructor
public class VehicleController {
    
    private final VehicleService vehicleService;
    
    @PostMapping
    public ResponseEntity<VehicleDTO> createVehicle(@Valid @RequestBody CreateVehicleRequest request) {
        VehicleDTO vehicle = vehicleService.createVehicle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicle);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<VehicleDTO> getVehicleById(@PathVariable Long id) {
        VehicleDTO vehicle = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(vehicle);
    }
    
    @GetMapping
    public ResponseEntity<List<VehicleDTO>> getAllVehicles() {
        List<VehicleDTO> vehicles = vehicleService.getAllVehicles();
        return ResponseEntity.ok(vehicles);
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<VehicleDTO>> getAvailableVehicles() {
        List<VehicleDTO> vehicles = vehicleService.getAvailableVehicles();
        return ResponseEntity.ok(vehicles);
    }
    
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<VehicleDTO>> getVehiclesByOwner(@PathVariable Long ownerId) {
        List<VehicleDTO> vehicles = vehicleService.getVehiclesByOwner(ownerId);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<VehicleDTO>> getVehiclesByDriver(@PathVariable Long driverId) {
        List<VehicleDTO> vehicles = vehicleService.getVehiclesByOwner(driverId);
        return ResponseEntity.ok(vehicles);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/calculate-cost")
    public ResponseEntity<Map<String, Object>> calculateRentalCost(@RequestBody Map<String, Integer> request) {
        Long vehicleId = Long.valueOf(request.get("vehicleId"));
        Integer hours = request.get("hours");
        Integer kilometers = request.get("kilometers");
        
        BigDecimal cost = vehicleService.calculateRentalCost(vehicleId, hours, kilometers);
        
        return ResponseEntity.ok(Map.of(
                "vehicleId", vehicleId,
                "hours", hours,
                "kilometers", kilometers,
                "totalCost", cost
        ));
    }
}

package com.carpooling.trip.client;

import com.carpooling.common.dto.VehicleSummaryDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class VehicleClientFallback implements VehicleClient {

    @Override
    public VehicleSummaryDTO getVehicleById(Long id) {
        log.warn("VehicleClient fallback acionado para veículo id={}", id);
        return VehicleSummaryDTO.builder()
                .id(id)
                .brand("Genérico")
                .model("Automóvel")
                .licensePlate("00-AA-00")
                .fuelConsumptionLPer100Km(5.5)
                .co2EmissionsGPerKm(120.0)
                .seatCapacity(4)
                .build();
    }

    @Override
    public List<VehicleSummaryDTO> getVehiclesByDriver(Long driverId) {
        log.warn("VehicleClient fallback acionado para driverId={}", driverId);
        return Collections.emptyList();
    }
}

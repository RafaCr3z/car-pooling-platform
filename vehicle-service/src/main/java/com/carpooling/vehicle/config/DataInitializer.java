package com.carpooling.vehicle.config;

import com.carpooling.vehicle.entity.Vehicle;
import com.carpooling.vehicle.entity.VehicleStatus;
import com.carpooling.vehicle.entity.VehicleType;
import com.carpooling.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final VehicleRepository vehicleRepository;

    @Override
    public void run(String... args) {
        if (vehicleRepository.count() > 0) {
            log.info("Base de dados de veículos já possui dados. Ignorando inicialização.");
            return;
        }

        log.info("A inicializar veículos de demonstração...");

        // Veículo do João Silva (ownerId = 2)
        Vehicle v1 = Vehicle.builder()
                .licensePlate("42-AB-89")
                .brand("Peugeot")
                .model("208 PureTech")
                .year(2021)
                .color("Cinzento")
                .seatCapacity(4)
                .fuelType("GASOLINE")
                .fuelConsumptionLPer100Km(5.2)
                .co2EmissionsGPerKm(118.0)
                .type(VehicleType.CAR)
                .ownerId(2L)
                .status(VehicleStatus.AVAILABLE)
                .pricePerHour(BigDecimal.ZERO)
                .pricePerKm(BigDecimal.ZERO)
                .location("Castelo Branco")
                .isDefault(true)
                .build();
        vehicleRepository.save(v1);

        // Veículo do Carlos Mendes (ownerId = 4)
        Vehicle v2 = Vehicle.builder()
                .licensePlate("78-CD-23")
                .brand("Renault")
                .model("Megane dCi")
                .year(2020)
                .color("Azul")
                .seatCapacity(4)
                .fuelType("DIESEL")
                .fuelConsumptionLPer100Km(4.8)
                .co2EmissionsGPerKm(110.0)
                .type(VehicleType.CAR)
                .ownerId(4L)
                .status(VehicleStatus.AVAILABLE)
                .pricePerHour(BigDecimal.ZERO)
                .pricePerKm(BigDecimal.ZERO)
                .location("Covilhã")
                .isDefault(true)
                .build();
        vehicleRepository.save(v2);

        log.info("Veículos de demonstração criados com sucesso: Peugeot 208 (42-AB-89) e Renault Megane (78-CD-23)");
    }
}

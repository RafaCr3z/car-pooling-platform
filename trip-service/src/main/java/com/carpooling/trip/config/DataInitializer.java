package com.carpooling.trip.config;

import com.carpooling.trip.entity.Trip;
import com.carpooling.trip.entity.TripStatus;
import com.carpooling.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@Profile({"!k8s & !prod"})
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final TripRepository tripRepository;

    @Override
    public void run(String... args) {
        try {
            initializeDemoTrips();
        } catch (DataIntegrityViolationException e) {
            log.warn("Viagens de demonstração já inseridas concorrentemente por outra réplica. Ignorando: {}", e.getMessage());
        }
    }

    private void initializeDemoTrips() {
        if (tripRepository.count() > 0) {
            log.info("Base de dados de viagens já possui dados. Ignorando inicialização.");
            return;
        }

        log.info("A inicializar viagens de demonstração...");

        // Viagem 1: Castelo Branco -> Lisboa
        Trip trip1 = Trip.builder()
                .driverId(2L)
                .driverName("João Silva")
                .vehicleId(1L)
                .vehicleInfo("Peugeot 208 PureTech (42-AB-89)")
                .departureLocation("Castelo Branco")
                .destinationLocation("Lisboa")
                .originLatitude(39.8222)
                .originLongitude(-7.4909)
                .destinationLatitude(38.7223)
                .destinationLongitude(-9.1393)
                .departureDateTime(LocalDateTime.now().plusDays(1).withHour(8).withMinute(30))
                .availableSeats(3)
                .occupiedSeats(1)
                .distance(225.0)
                .estimatedFuelCost(BigDecimal.valueOf(19.80))
                .estimatedTolls(BigDecimal.valueOf(12.50))
                .totalCost(BigDecimal.valueOf(32.30))
                .costPerPassenger(BigDecimal.valueOf(10.77))
                .co2SavedKg(25.8)
                .description("Saída perto da estação de comboios de Castelo Branco. Chegada ao Campo Grande em Lisboa. Música calma.")
                .status(TripStatus.OPEN)
                .build();
        tripRepository.save(trip1);

        // Viagem 2: Covilhã -> Porto
        Trip trip2 = Trip.builder()
                .driverId(4L)
                .driverName("Carlos Mendes")
                .vehicleId(2L)
                .vehicleInfo("Renault Megane dCi (78-CD-23)")
                .departureLocation("Covilhã")
                .destinationLocation("Porto")
                .originLatitude(40.2827)
                .originLongitude(-7.5033)
                .destinationLatitude(41.1579)
                .destinationLongitude(-8.6291)
                .departureDateTime(LocalDateTime.now().plusDays(2).withHour(14).withMinute(0))
                .availableSeats(3)
                .occupiedSeats(0)
                .distance(245.0)
                .estimatedFuelCost(BigDecimal.valueOf(20.50))
                .estimatedTolls(BigDecimal.valueOf(11.80))
                .totalCost(BigDecimal.valueOf(32.30))
                .costPerPassenger(BigDecimal.valueOf(10.77))
                .co2SavedKg(28.2)
                .description("Saída da Central de Camionagem da Covilhã com destino ao Polo Universitário do Porto.")
                .status(TripStatus.OPEN)
                .build();
        tripRepository.save(trip2);

        log.info("Viagens de demonstração criadas com sucesso (Castelo Branco -> Lisboa e Covilhã -> Porto)");
    }
}

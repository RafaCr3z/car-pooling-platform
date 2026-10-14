package com.carpooling.vehicle;

import com.carpooling.vehicle.dto.CreateVehicleRequest;
import com.carpooling.vehicle.dto.VehicleDTO;
import com.carpooling.vehicle.entity.Vehicle;
import com.carpooling.vehicle.entity.VehicleStatus;
import com.carpooling.vehicle.entity.VehicleType;
import com.carpooling.vehicle.repository.VehicleRepository;
import com.carpooling.vehicle.service.VehicleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    @Test
    @DisplayName("Deve criar veículo com valores padrão para consumo e emissões de CO2")
    void testCreateVehicle_DefaultValues() {
        CreateVehicleRequest req = CreateVehicleRequest.builder()
                .licensePlate("aa-00-bb")
                .brand("Peugeot")
                .model("208")
                .year(2022)
                .ownerId(10L)
                .build();

        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> {
            Vehicle v = invocation.getArgument(0);
            v.setId(1L);
            return v;
        });

        VehicleDTO result = vehicleService.createVehicle(req);

        assertNotNull(result);
        assertEquals("AA-00-BB", result.getLicensePlate());
        assertEquals("Peugeot", result.getBrand());
        assertEquals(5.5, result.getFuelConsumptionLPer100Km());
        assertEquals(115.0, result.getCo2EmissionsGPerKm());
        assertEquals("AVAILABLE", result.getStatus());
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Deve registar veículo elétrico com emissões zero")
    void testCreateVehicle_ElectricVehicle() {
        CreateVehicleRequest req = CreateVehicleRequest.builder()
                .licensePlate("77-zz-99")
                .brand("Tesla")
                .model("Model 3")
                .year(2023)
                .fuelType("ELECTRIC")
                .fuelConsumptionLPer100Km(0.0)
                .co2EmissionsGPerKm(0.0)
                .ownerId(10L)
                .seatCapacity(5)
                .build();

        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> {
            Vehicle v = invocation.getArgument(0);
            v.setId(2L);
            return v;
        });

        VehicleDTO result = vehicleService.createVehicle(req);

        assertNotNull(result);
        assertEquals("77-ZZ-99", result.getLicensePlate());
        assertEquals(0.0, result.getFuelConsumptionLPer100Km());
        assertEquals(0.0, result.getCo2EmissionsGPerKm());
        assertEquals("ELECTRIC", result.getFuelType());
    }

    @Test
    @DisplayName("Deve filtrar veículos pertencentes a um determinado condutor")
    void testGetVehiclesByOwner() {
        Vehicle v1 = Vehicle.builder().id(1L).brand("Renault").model("Clio").ownerId(5L).status(VehicleStatus.AVAILABLE).build();
        Vehicle v2 = Vehicle.builder().id(2L).brand("Peugeot").model("208").ownerId(5L).status(VehicleStatus.AVAILABLE).build();

        when(vehicleRepository.findByOwnerId(5L)).thenReturn(List.of(v1, v2));

        List<VehicleDTO> result = vehicleService.getVehiclesByOwner(5L);

        assertEquals(2, result.size());
        assertEquals("Renault", result.get(0).getBrand());
        assertEquals("Peugeot", result.get(1).getBrand());
    }

    @Test
    @DisplayName("Deve atualizar estado do veículo corretamente")
    void testUpdateVehicleStatus() {
        Vehicle v = Vehicle.builder().id(1L).brand("Renault").status(VehicleStatus.AVAILABLE).build();
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(v));

        vehicleService.updateVehicleStatus(1L, VehicleStatus.MAINTENANCE);

        assertEquals(VehicleStatus.MAINTENANCE, v.getStatus());
        verify(vehicleRepository).save(v);
    }
}

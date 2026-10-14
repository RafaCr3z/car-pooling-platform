package com.carpooling.location;

import com.carpooling.location.service.LocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - LocationService (Cálculo de Distâncias, Rotas e Cidades)")
class LocationServiceTest {

    private LocationService locationService;

    @BeforeEach
    void setUp() {
        locationService = new LocationService();
    }

    @Test
    @DisplayName("Deve encontrar cidade por nome exato e maiúsculas/minúsculas")
    void testFindCity() {
        Optional<LocationService.CityLocation> city = locationService.findCity("castelo branco");
        assertTrue(city.isPresent(), "Castelo Branco deve ser encontrada");
        assertEquals("Castelo Branco", city.get().name());
        assertEquals(39.8222, city.get().lat(), 0.0001);
        assertEquals(-7.4909, city.get().lon(), 0.0001);
    }

    @Test
    @DisplayName("Deve calcular distância geodésica aproximada entre Lisboa e Porto")
    void testCalculateDistance() {
        // Lisboa (38.7223, -9.1393) -> Porto (41.1579, -8.6291)
        Double distance = locationService.calculateDistance(
                BigDecimal.valueOf(38.7223), BigDecimal.valueOf(-9.1393),
                BigDecimal.valueOf(41.1579), BigDecimal.valueOf(-8.6291)
        );

        assertNotNull(distance);
        // Distância rodoviária real é cerca de 315km, fórmula com fator rodoviário 1.22 deve dar entre 300 e 350km
        assertTrue(distance > 300 && distance < 360, "Distância Lisboa-Porto deve ser razoável (~335km)");
    }

    @Test
    @DisplayName("Deve estimar o tempo de viagem proporcional à distância")
    void testEstimateTravelTime() {
        Long minutes = locationService.estimateTravelTime(170.0);
        // 170km a 85km/h = 2 horas = 120 minutos
        assertNotNull(minutes);
        assertEquals(120L, minutes);
    }

    @Test
    @DisplayName("Deve calcular rota completa entre Castelo Branco e Coimbra com portagens e combustível")
    void testCalculateRouteBetweenCities() {
        Map<String, Object> route = locationService.calculateRouteBetweenCities("Castelo Branco", "Coimbra");

        assertNotNull(route);
        assertEquals("Castelo Branco", route.get("origin"));
        assertEquals("Coimbra", route.get("destination"));
        assertTrue(((Number) route.get("distanceKm")).doubleValue() > 90.0, "Distância deve ser superior a 90km");
        assertTrue(((Number) route.get("estimatedFuelCost")).doubleValue() > 5.0, "Combustível deve ser positivo");
        assertTrue(((Number) route.get("estimatedTolls")).doubleValue() > 0.0, "Portagens estimadas devem ser positivas");
        assertTrue(((Number) route.get("totalEstimatedCost")).doubleValue() > 10.0, "Custo total deve incluir combustível e portagens");
    }

    @Test
    @DisplayName("Deve listar catálogo com as principais cidades portuguesas")
    void testGetAllCities() {
        var cities = locationService.getAllCities();
        assertNotNull(cities);
        assertTrue(cities.size() >= 15, "Catálogo deve conter pelo menos 15 cidades portuguesas");
    }
}

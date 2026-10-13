package com.carpooling.admin.client.fallback;

import com.carpooling.admin.client.TripServiceClient;
import com.carpooling.admin.dto.TripResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class TripClientFallback implements TripServiceClient {

    @Override
    public List<TripResponseDTO> getAllTrips() {
        log.warn("Fallback ativado: Trip Service indisponível para getAllTrips");
        return Collections.emptyList();
    }

    @Override
    public TripResponseDTO updateTripStatus(Long id, String status) {
        log.warn("Fallback ativado: Trip Service indisponível para updateTripStatus id={} status={}", id, status);
        return null;
    }
}

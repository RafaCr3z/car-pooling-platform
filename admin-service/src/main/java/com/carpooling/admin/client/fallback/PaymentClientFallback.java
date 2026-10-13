package com.carpooling.admin.client.fallback;

import com.carpooling.admin.client.PaymentServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
@Slf4j
public class PaymentClientFallback implements PaymentServiceClient {

    @Override
    public Map<String, Object> getTotalTransacted() {
        log.warn("Fallback ativado: Payment Service indisponível para getTotalTransacted");
        return Map.of("totalAmount", BigDecimal.ZERO);
    }
}

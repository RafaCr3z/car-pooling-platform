package com.carpooling.admin.client;

import com.carpooling.admin.client.fallback.PaymentClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
import java.util.Map;

@FeignClient(name = "payment-service", fallback = PaymentClientFallback.class)
public interface PaymentServiceClient {

    @GetMapping("/api/payments/total")
    Map<String, Object> getTotalTransacted();
}

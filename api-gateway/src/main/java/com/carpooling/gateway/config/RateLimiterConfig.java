package com.carpooling.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Configuration
public class RateLimiterConfig {

    /**
     * Resolve a chave do cliente com base no endereço IP real
     * (respeitando o cabeçalho X-Forwarded-For gerado por proxies reversos).
     */
    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String clientIp = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (clientIp == null || clientIp.isEmpty()) {
                clientIp = Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                        .map(address -> address.getAddress().getHostAddress())
                        .orElse("unknown-client");
            } else if (clientIp.contains(",")) {
                // Se houver múltiplos proxies, o primeiro IP é o cliente original
                clientIp = clientIp.split(",")[0].trim();
            }
            return Mono.just(clientIp);
        };
    }
}

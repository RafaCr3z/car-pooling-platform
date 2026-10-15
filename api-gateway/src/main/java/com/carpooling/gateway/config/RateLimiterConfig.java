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
     * Resolve a chave do cliente com base no endereço IP real.
     * Para prevenir IP spoofing via cabeçalho manipulado pelo cliente:
     * 1. Verifica X-Real-IP definido pelo Ingress/Proxy de borda confiável.
     * 2. Caso existam múltiplos hops no X-Forwarded-For, utiliza o último hop (adicionado pelo proxy/ingress imediatamente anterior).
     * 3. Fallback seguro para o RemoteAddress direto da conexão TCP.
     */
    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String clientIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
            if (clientIp == null || clientIp.isBlank()) {
                String xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
                if (xff != null && !xff.isBlank()) {
                    String[] parts = xff.split(",");
                    // O último IP da cadeia é o que foi anexado pelo proxy confiável anterior
                    clientIp = parts[parts.length - 1].trim();
                }
            }
            if (clientIp == null || clientIp.isBlank()) {
                clientIp = Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                        .map(address -> address.getAddress().getHostAddress())
                        .orElse("unknown-client");
            }
            return Mono.just(clientIp);
        };
    }
}

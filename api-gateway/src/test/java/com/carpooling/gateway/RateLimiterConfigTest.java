package com.carpooling.gateway;

import com.carpooling.gateway.config.RateLimiterConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;

class RateLimiterConfigTest {

    private final RateLimiterConfig config = new RateLimiterConfig();
    private final KeyResolver resolver = config.ipKeyResolver();

    @Test
    @DisplayName("Deve extrair endereço IP do cabeçalho X-Forwarded-For")
    void testResolveIpFromXForwardedFor() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/trips")
                .header("X-Forwarded-For", "203.0.113.195")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(resolver.resolve(exchange))
                .expectNext("203.0.113.195")
                .verifyComplete();
    }

    @Test
    @DisplayName("Deve extrair o último IP confiável quando existirem múltiplos proxies no X-Forwarded-For (evitando spoofing)")
    void testResolveTrustedIpFromMultipleProxies() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/trips")
                .header("X-Forwarded-For", "198.51.100.10, 10.0.0.1, 172.16.0.2")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(resolver.resolve(exchange))
                .expectNext("172.16.0.2")
                .verifyComplete();
    }

    @Test
    @DisplayName("Deve priorizar X-Real-IP quando fornecido pelo Ingress")
    void testResolveIpFromXRealIp() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/trips")
                .header("X-Real-IP", "203.0.113.88")
                .header("X-Forwarded-For", "198.51.100.10, 10.0.0.1")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(resolver.resolve(exchange))
                .expectNext("203.0.113.88")
                .verifyComplete();
    }

    @Test
    @DisplayName("Deve usar o remoteAddress quando o cabeçalho X-Forwarded-For não estiver presente")
    void testResolveIpFromRemoteAddress() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/trips")
                .remoteAddress(new InetSocketAddress("192.168.1.50", 12345))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(resolver.resolve(exchange))
                .expectNext("192.168.1.50")
                .verifyComplete();
    }

    @Test
    @DisplayName("Deve retornar fallback 'unknown-client' quando não existir header nem remoteAddress")
    void testResolveFallbackWhenNoHeadersOrAddress() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/trips").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(resolver.resolve(exchange))
                .expectNext("unknown-client")
                .verifyComplete();
    }

    @Test
    @DisplayName("Deve resolver IP local 127.0.0.1 corretamente")
    void testResolveLocalhostAddress() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/trips")
                .remoteAddress(new InetSocketAddress("127.0.0.1", 8080))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(resolver.resolve(exchange))
                .expectNext("127.0.0.1")
                .verifyComplete();
    }
}


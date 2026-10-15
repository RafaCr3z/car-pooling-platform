package com.carpooling.gateway;

import com.carpooling.gateway.filter.JwtAuthenticationFilter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "MyVeryLongSecretKeyFor256BitHS256AlgorithmThatIsAtLeast32Characters";
    private JwtAuthenticationFilter filter;
    private GatewayFilterChain chain;
    private SecretKey key;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(SECRET);
        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
        key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    private String createToken(String email, Long userId, List<String> roles, long expirationOffset) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationOffset))
                .signWith(key)
                .compact();
    }

    @Test
    @DisplayName("Deve permitir rotas públicas sem token de autenticação")
    void testPublicPathAllowed() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/auth/login").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain, times(1)).filter(exchange);
    }

    @Test
    @DisplayName("Deve rejeitar rota protegida sem cabeçalho Authorization com status 401")
    void testProtectedPathWithoutTokenReturns401() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/trips").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    @DisplayName("Deve rejeitar rota protegida com token expirado ou inválido com status 401")
    void testInvalidTokenReturns401() {
        String expiredToken = createToken("user@test.com", 1L, List.of("PASSENGER"), -10000);
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/trips")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    @DisplayName("Deve rejeitar acesso de utilizador sem papel ADMIN a /api/admin/** com status 403")
    void testNonAdminAccessToAdminRouteReturns403() {
        String token = createToken("passenger@test.com", 2L, List.of("PASSENGER"), 3600000);
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/admin/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    @DisplayName("Deve permitir acesso de ADMIN a /api/admin/** e injetar cabeçalhos X-User-* downstream")
    void testAdminAccessAllowedAndHeadersInjected() {
        String token = createToken("admin@carpooling.pt", 99L, List.of("ADMIN"), 3600000);
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/admin/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("X-User-Id", "spoofed-123") // Tenta spoofing
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(any(ServerWebExchange.class))).thenAnswer(invocation -> {
            ServerWebExchange mutatedExchange = invocation.getArgument(0);
            HttpHeaders headers = mutatedExchange.getRequest().getHeaders();
            assertEquals("99", headers.getFirst("X-User-Id")); // Sanitizado!
            assertEquals("admin@carpooling.pt", headers.getFirst("X-User-Email"));
            assertEquals("ADMIN", headers.getFirst("X-User-Roles"));
            return Mono.empty();
        });

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain, times(1)).filter(any());
    }
}

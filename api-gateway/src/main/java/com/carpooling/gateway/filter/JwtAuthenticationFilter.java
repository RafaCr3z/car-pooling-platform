package com.carpooling.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Filtro global de autenticação e autorização JWT na borda (API Gateway).
 *
 * <p>Responsabilidades fundamentais:</p>
 * <ul>
 *   <li>Permitir rotas públicas (whitelist: login, registo, documentação Swagger, healthchecks).</li>
 *   <li>Exigir Bearer Token válido em todas as restantes rotas protegidas (retornando 401 se ausente ou inválido).</li>
 *   <li>Controlo de Acesso Baseado em Papéis (RBAC): exigir explicitamente a role {@code ADMIN}
 *       para quaisquer acessos a rotas {@code /api/admin/**} (retornando 403 se o utilizador não for admin).</li>
 *   <li>Limpar cabeçalhos externos {@code X-User-*} potencialmente forjados pelo cliente e propagar
 *       a jusante (downstream) os dados confiáveis do utilizador (ID, email, roles).</li>
 * </ul>
 */
@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    public static final String ROLES_CLAIM = "roles";
    public static final String USER_ID_CLAIM = "userId";

    private final SecretKey signingKey;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/actuator",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-ui.html",
            "/webjars"
    );

    public JwtAuthenticationFilter(@Value("${jwt.secret:MyVeryLongSecretKeyFor256BitHS256AlgorithmThatIsAtLeast32Characters}") String jwtSecret) {
        if (jwtSecret == null || jwtSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET deve ter pelo menos 32 caracteres (256 bits) para HMAC-SHA256.");
        }
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. CORS Preflight (OPTIONS) e rotas públicas são permitidos sem token
        if (request.getMethod() == HttpMethod.OPTIONS || isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // 2. Extrair cabeçalho Authorization
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Tentativa de acesso não autenticado bloqueada no Gateway: path={}", path);
            return unauthorizedResponse(exchange, "Autenticação obrigatória: Bearer token ausente");
        }

        String token = authHeader.substring(7).trim();

        // 3. Validar assinatura e expiração do JWT
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.warn("Token JWT inválido ou expirado rejeitado no Gateway para path={}: {}", path, e.getMessage());
            return unauthorizedResponse(exchange, "Token JWT inválido ou expirado");
        }

        // 4. Extrair claims de identidade e permissões
        String email = claims.getSubject();
        Object rawUserId = claims.get(USER_ID_CLAIM);
        String userIdStr = rawUserId != null ? rawUserId.toString() : "";

        @SuppressWarnings("unchecked")
        List<String> roles = claims.get(ROLES_CLAIM, List.class);
        boolean isAdmin = roles != null && roles.stream().anyMatch(r -> "ADMIN".equalsIgnoreCase(r.toString()));

        // 5. RBAC: Proteger rotas administrativas (/api/admin/**)
        if (path.startsWith("/api/admin")) {
            if (!isAdmin) {
                log.warn("Tentativa de acesso não autorizado a rota de admin: user={} path={}", email, path);
                return forbiddenResponse(exchange, "Acesso restrito: permissão de Administrador (ADMIN) necessária");
            }
        }

        // 6. Sanitizar e propagar cabeçalhos confiáveis downstream (evita spoofing pelo cliente)
        ServerHttpRequest.Builder requestBuilder = request.mutate()
                .headers(httpHeaders -> {
                    httpHeaders.remove("X-User-Id");
                    httpHeaders.remove("X-User-Email");
                    httpHeaders.remove("X-User-Roles");
                })
                .header("X-User-Id", userIdStr)
                .header("X-User-Email", email != null ? email : "")
                .header("X-User-Roles", roles != null ? String.join(",", roles) : "");

        return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes = ("{\"error\":\"UNAUTHORIZED\",\"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    private Mono<Void> forbiddenResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes = ("{\"error\":\"FORBIDDEN\",\"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // Alta prioridade: valida antes de qualquer roteamento ou rate limiting
    }
}

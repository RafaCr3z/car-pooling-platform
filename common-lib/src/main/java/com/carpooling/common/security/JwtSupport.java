package com.carpooling.common.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

/**
 * Configuracao partilhada de validacao de JWT para todos os microsservicos.
 *
 * <p>Os tokens sao emitidos pelo auth-service e assinados com HMAC-SHA256 (HS256).
 * O gateway e cada servico validam assinatura e expiracao de forma independente
 * (defesa em profundidade / zero-trust): um pedido que contorne o gateway
 * continua a precisar de um token valido.</p>
 */
public final class JwtSupport {

    /** Claim onde o auth-service coloca os papeis do utilizador (ex.: ["ADMIN"]). */
    public static final String ROLES_CLAIM = "roles";
    /** Claim com o identificador numerico do utilizador. */
    public static final String USER_ID_CLAIM = "userId";
    /** HS256 exige uma chave de pelo menos 256 bits. */
    public static final int MIN_SECRET_BYTES = 32;

    private JwtSupport() {
    }

    /**
     * Cria a chave HMAC a partir do segredo partilhado, falhando no arranque
     * se o segredo estiver ausente ou for demasiado curto (fail-fast).
     */
    public static SecretKey hmacKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET nao definido. Defina a variavel de ambiente JWT_SECRET (ver .env.example).");
        }
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "JWT_SECRET demasiado curto: HS256 exige pelo menos " + MIN_SECRET_BYTES + " bytes.");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    /** Decoder para servicos servlet (Spring MVC). */
    public static JwtDecoder servletDecoder(String secret) {
        return NimbusJwtDecoder.withSecretKey(hmacKey(secret))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    /** Decoder para o API Gateway (WebFlux). */
    public static ReactiveJwtDecoder reactiveDecoder(String secret) {
        return NimbusReactiveJwtDecoder.withSecretKey(hmacKey(secret))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    /** Converte a claim {@code roles} em authorities {@code ROLE_*}. */
    public static Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList(ROLES_CLAIM);
        if (roles == null) {
            return List.of();
        }
        return roles.stream()
                .filter(r -> r != null && !r.isBlank())
                .map(r -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + r.trim().toUpperCase()))
                .toList();
    }

    /** Converter usado por {@code oauth2ResourceServer().jwt()} nos servicos servlet. */
    public static JwtAuthenticationConverter authenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        Converter<Jwt, Collection<GrantedAuthority>> authorities = JwtSupport::extractAuthorities;
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }
}

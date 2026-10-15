package com.carpooling.auth.security;

import com.carpooling.auth.entity.User;
import com.carpooling.common.security.JwtSupport;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Emissor de JWT (HS256).
 *
 * <p>O segredo vem obrigatoriamente da variavel de ambiente {@code JWT_SECRET}
 * (sem valor por defeito no codigo). O algoritmo e fixado explicitamente em HS256
 * para ser identico ao que o gateway e os restantes servicos aceitam.</p>
 */
@Component
@Slf4j
public class JwtTokenProvider {

    private final SecretKey signingKey;
    private final long jwtExpirationMs;

    public JwtTokenProvider(@Value("${jwt.secret}") String jwtSecret,
                            @Value("${jwt.expiration:3600000}") long jwtExpirationMs) {
        this.signingKey = JwtSupport.hmacKey(jwtSecret);
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public String generateToken(User user) {
        List<String> roleNames = user.getRoles() != null
                ? user.getRoles().stream().map(Enum::name).collect(Collectors.toList())
                : List.of("PASSENGER");

        return Jwts.builder()
                .subject(user.getEmail())
                .claim(JwtSupport.USER_ID_CLAIM, user.getId())
                .claim("fullName", user.getFullName())
                .claim(JwtSupport.ROLES_CLAIM, roleNames)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        Object val = getClaims(token).get(JwtSupport.USER_ID_CLAIM);
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
}

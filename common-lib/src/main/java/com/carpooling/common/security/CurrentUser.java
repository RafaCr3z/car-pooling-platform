package com.carpooling.common.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

/**
 * Identidade do utilizador autenticado, extraida <b>exclusivamente</b> do JWT validado.
 *
 * <p>Nunca se confia em identificadores enviados no corpo, query string ou cabecalhos
 * do pedido para decidir "quem" esta a fazer a operacao.</p>
 */
public record CurrentUser(Long userId, String email, List<String> roles) {

    public static final String ADMIN = "ADMIN";

    public static CurrentUser from(Jwt jwt) {
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Autenticacao obrigatoria");
        }
        Object rawId = jwt.getClaim(JwtSupport.USER_ID_CLAIM);
        Long id = null;
        if (rawId instanceof Number n) {
            id = n.longValue();
        } else if (rawId instanceof String s && !s.isBlank()) {
            try {
                id = Long.parseLong(s);
            } catch (NumberFormatException ignored) {
                // claim invalida: tratada como ausente
            }
        }
        List<String> roles = jwt.getClaimAsStringList(JwtSupport.ROLES_CLAIM);
        return new CurrentUser(id, jwt.getSubject(), roles == null ? List.of() : List.copyOf(roles));
    }

    public boolean isAdmin() {
        return roles.stream().anyMatch(ADMIN::equalsIgnoreCase);
    }

    public boolean is(Long otherUserId) {
        return userId != null && Objects.equals(userId, otherUserId);
    }

    /**
     * Garante que o utilizador autenticado e o dono do recurso, ou um administrador.
     * Protege contra IDOR (Insecure Direct Object Reference).
     *
     * @throws ResponseStatusException 403 se nao for dono nem ADMIN
     */
    public void requireSelfOrAdmin(Long ownerId) {
        if (!isAdmin() && !is(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Sem permissao para aceder a recursos de outro utilizador");
        }
    }

    public void requireAdmin() {
        if (!isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operacao reservada a administradores");
        }
    }

    /** Variante por email (subject do token). */
    public void requireSelfOrAdmin(String ownerEmail) {
        if (!isAdmin() && (email == null || !email.equalsIgnoreCase(ownerEmail))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Sem permissao para aceder a recursos de outro utilizador");
        }
    }
}

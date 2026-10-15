package com.carpooling.common.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Propaga o JWT do utilizador nas chamadas OpenFeign entre servicos.
 *
 * <p>Assim o servico de destino aplica as suas proprias regras de autorizacao
 * com a identidade real de quem originou o pedido (nao existe "confianca implicita"
 * dentro do cluster).</p>
 *
 * <p>Nota: o SecurityContext e thread-local. Com o circuit breaker do Feign ativo e
 * necessario {@code spring.cloud.circuitbreaker.resilience4j.disable-thread-pool=true}
 * para a chamada correr na mesma thread do pedido original.</p>
 */
public class BearerTokenRelayInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth && !template.headers().containsKey(HttpHeaders.AUTHORIZATION)) {
            template.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtAuth.getToken().getTokenValue());
        }
    }
}

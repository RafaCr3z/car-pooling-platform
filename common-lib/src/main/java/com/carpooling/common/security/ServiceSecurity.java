package com.carpooling.common.security;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Cadeia de seguranca base para os microsservicos servlet.
 *
 * <ul>
 *   <li>Stateless (sem sessao HTTP) e sem CSRF: a API so aceita Bearer tokens, nao cookies.</li>
 *   <li>Endpoints de infraestrutura publicos: health/info/prometheus e OpenAPI.</li>
 *   <li>Regras especificas do servico aplicadas a seguir.</li>
 *   <li>Tudo o resto exige um JWT valido ({@code anyRequest().authenticated()}).</li>
 * </ul>
 */
public final class ServiceSecurity {

    public static final String[] INFRA_PUBLIC_PATHS = {
            "/actuator/health", "/actuator/health/**", "/actuator/info", "/actuator/prometheus",
            "/v3/api-docs", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**"
    };

    private ServiceSecurity() {
    }

    public static SecurityFilterChain build(
            HttpSecurity http,
            JwtDecoder decoder,
            Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> serviceRules)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable())
                .logout(l -> l.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(INFRA_PUBLIC_PATHS).permitAll();
                    serviceRules.customize(auth);
                    auth.anyRequest().authenticated();
                })
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt
                        .decoder(decoder)
                        .jwtAuthenticationConverter(JwtSupport.authenticationConverter())));
        return http.build();
    }
}

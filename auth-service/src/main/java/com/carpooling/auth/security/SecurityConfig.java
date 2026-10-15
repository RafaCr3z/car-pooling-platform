package com.carpooling.auth.security;

import com.carpooling.common.security.JwtSupport;
import com.carpooling.common.security.ServiceSecurity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${jwt.secret:MyVeryLongSecretKeyFor256BitHS256AlgorithmThatIsAtLeast32Characters}")
    private String jwtSecret;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return JwtSupport.servletDecoder(jwtSecret);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return ServiceSecurity.build(http, jwtDecoder(), auth -> auth
                // Endpoints públicos de login, registo e pesquisa de condutores
                .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/drivers").permitAll()
                // Gestão de utilizadores restrita a ADMIN
                .requestMatchers(HttpMethod.GET, "/api/auth/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/auth/user/*/block", "/api/auth/user/*/unblock").hasRole("ADMIN")
        );
    }
}


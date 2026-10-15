package com.carpooling.location.config;

import com.carpooling.common.security.JwtSupport;
import com.carpooling.common.security.ServiceSecurity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${jwt.secret:MyVeryLongSecretKeyFor256BitHS256AlgorithmThatIsAtLeast32Characters}")
    private String jwtSecret;

    @Bean
    public JwtDecoder jwtDecoder() {
        return JwtSupport.servletDecoder(jwtSecret);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return ServiceSecurity.build(http, jwtDecoder(), auth -> auth
                .requestMatchers("/api/locations/**").permitAll()
                .anyRequest().authenticated()
        );
    }
}

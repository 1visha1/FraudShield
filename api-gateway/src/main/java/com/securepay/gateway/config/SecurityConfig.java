package com.securepay.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeExchange(exchanges -> exchanges
                // Public endpoints — no authentication required
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/api/auth/login", "/api/auth/register").permitAll()
                .pathMatchers("/oauth2/**", "/.well-known/**").permitAll()

                // Scope-based access control per service
                .pathMatchers("/api/v1/transactions/**").hasAuthority("SCOPE_write")
                .pathMatchers("/api/v1/devices/**").hasAuthority("SCOPE_write")
                .pathMatchers("/api/v1/audit/**").hasAuthority("SCOPE_read")
                .pathMatchers("/api/v1/rules/**").hasAuthority("SCOPE_read")
                .pathMatchers("/api/v1/risk/**").hasAuthority("SCOPE_read")
                .pathMatchers("/api/v1/fraud/**").hasAuthority("SCOPE_read")
                .pathMatchers("/api/v1/otp/**").hasAuthority("SCOPE_write")
                .pathMatchers("/api/v1/auth/**").hasAuthority("SCOPE_write")

                // Everything else requires authentication
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions
                    .mode(XFrameOptionsServerHttpHeadersWriter.Mode.DENY))
                .cache(ServerHttpSecurity.HeaderSpec.CacheSpec::disable)
            );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "http://localhost:4200",
            "http://localhost:8080"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-Correlation-Id",
            "X-Requested-With", "Accept", "Origin"
        ));
        configuration.setExposedHeaders(Arrays.asList(
            "X-Correlation-Id", "Authorization"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
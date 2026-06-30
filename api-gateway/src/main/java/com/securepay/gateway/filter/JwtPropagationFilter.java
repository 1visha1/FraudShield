package com.securepay.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Component
public class JwtPropagationFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
            .map(securityContext -> securityContext.getAuthentication())
            .filter(authentication -> authentication instanceof JwtAuthenticationToken)
            .cast(JwtAuthenticationToken.class)
            .flatMap(jwtAuth -> {
                Jwt jwt = jwtAuth.getToken();

                // Collect roles/authorities for downstream services
                String roles = jwtAuth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));

                ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(builder -> builder
                        // Forward the original Authorization header so downstream services
                        // can independently validate the JWT if needed
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt.getTokenValue())
                        .header("X-User-Id", jwt.getSubject())
                        .header("X-Username", jwt.getClaimAsString("sub"))
                        .header("X-Customer-Id", jwt.getClaimAsString("customerId") != null
                            ? jwt.getClaimAsString("customerId") : "")
                        .header("X-User-Roles", roles)
                    )
                    .build();
                return chain.filter(mutatedExchange);
            })
            .switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        return 10; // Execute after CorrelationIdFilter
    }
}
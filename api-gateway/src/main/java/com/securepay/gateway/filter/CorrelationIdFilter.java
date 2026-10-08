package com.fraudshield.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Slf4j
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders requestHeaders = exchange.getRequest().getHeaders();

        String correlationId;
        if (requestHeaders.containsKey(CORRELATION_ID_HEADER)) {
            correlationId = requestHeaders.getFirst(CORRELATION_ID_HEADER);
            log.debug("Found existing Correlation-Id: {}", correlationId);
        } else {
            correlationId = UUID.randomUUID().toString();
            log.debug("Generated new Correlation-Id: {}", correlationId);
            
            // Add to request headers so downstream services can see it
            exchange = exchange.mutate()
                    .request(r -> r.header(CORRELATION_ID_HEADER, correlationId))
                    .build();
        }

        // Add to response headers so client can see it
        exchange.getResponse().getHeaders().add(CORRELATION_ID_HEADER, correlationId);

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1; // Execute early in the filter chain
    }
}
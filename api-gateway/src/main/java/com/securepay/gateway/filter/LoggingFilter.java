package com.securepay.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
@Slf4j
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();
        String correlationId = exchange.getRequest().getHeaders().getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER);

        log.info("Incoming Request: {} {} | CorrelationId: {}", method, path, correlationId);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            
            // Extract route information
            Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            String serviceId = (route != null) ? route.getId() : "Unknown";
            
            Integer statusCode = (exchange.getResponse().getStatusCode() != null) 
                                ? exchange.getResponse().getStatusCode().value() 
                                : null;

            log.info("Response: {} | Service: {} | Duration: {}ms | CorrelationId: {}", 
                    statusCode, serviceId, duration, correlationId);
        }));
    }

    @Override
    public int getOrder() {
        return 0; // Execute after CorrelationIdFilter
    }
}
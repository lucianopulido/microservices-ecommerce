package com.ecommerce.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("product-service", r -> r
                        .path("/api/v1/product", "/api/v1/product/**")
                        .filters(f -> f.tokenRelay())
                        .uri("lb://product-service"))
                .route("order-service", r -> r
                        .path("/api/v1/order", "/api/v1/order/**")
                        .filters(f -> f.tokenRelay())
                        .uri("lb://order-service"))
                .route("inventory-service", r -> r
                        .path("/api/v1/inventory", "/api/v1/inventory/**")
                        .filters(f -> f.tokenRelay())
                        .uri("lb://inventory-service"))
                .build();
    }
}

package com.ipo.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // IPO Application Service Routes
                .route("app_service_route", r -> r
                        .path("/api/v1/ipo/**")
                        .uri("lb://IPO-APPLICATION-SERVICE"))

                // Payment Service Routes
                .route("payment_service_route", r -> r
                        .path("/api/v1/payments/**")
                        .uri("lb://IPO-PAYMENT-SERVICE"))

                // Payment Webhook Route
                .route("payment_webhook_route", r -> r
                        .path("/webhook/**")
                        .uri("lb://IPO-PAYMENT-SERVICE"))

                // Allotment Service Routes
                .route("allotment_trigger_route", r -> r
                        .path("/api/v1/allotment/**")
                        .uri("lb://IPO-ALLOTMENT-SERVICE"))

                // Lottery Route
                .route("lottery_route", r -> r
                        .path("/allotment/**")
                        .uri("lb://IPO-ALLOTMENT-SERVICE"))

                .build();
    }
}
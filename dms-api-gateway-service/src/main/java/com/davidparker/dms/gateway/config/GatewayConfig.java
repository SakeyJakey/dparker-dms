package com.davidparker.dms.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("admin-service", r -> r
                .path("/api/admin/**")
                .uri("${dms.services.admin-service-url:http://dms-admin-service:8080}"))
            .route("audit-service", r -> r
                .path("/api/audit/**")
                .uri("${dms.services.audit-service-url:http://dms-audit-service:8080}"))
            .route("document-service", r -> r
                .path("/api/documents/**")
                .uri("${dms.services.document-service-url:http://dms-document-service:8080}"))
            .route("compliance-service", r -> r
                .path("/api/compliance/**")
                .uri("${dms.services.compliance-service-url:http://dms-compliance-service:8080}"))
            .route("llm-service", r -> r
                .path("/api/llm/**")
                .uri("${dms.services.llm-service-url:http://dms-llm-service:8080}"))
            .build();
    }
}

package com.davidparker.dms.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Value("${dms.services.admin-service-url:http://dms-admin-service:8080}")
    private String adminServiceUrl;

    @Value("${dms.services.audit-service-url:http://dms-audit-service:8080}")
    private String auditServiceUrl;

    @Value("${dms.services.document-service-url:http://dms-document-service:8080}")
    private String documentServiceUrl;

    @Value("${dms.services.compliance-service-url:http://dms-compliance-service:8080}")
    private String complianceServiceUrl;

    @Value("${dms.services.llm-service-url:http://dms-llm-service:8080}")
    private String llmServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("admin-service", r -> r
                .path("/api/v1/admin/**")
                .uri(adminServiceUrl))
            .route("audit-service", r -> r
                .path("/api/v1/audit/**")
                .uri(auditServiceUrl))
            .route("document-service", r -> r
                .path("/api/v1/documents/**")
                .uri(documentServiceUrl))
            .route("compliance-service", r -> r
                .path("/api/v1/compliance/**")
                .uri(complianceServiceUrl))
            .route("llm-service", r -> r
                .path("/api/v1/llm/**")
                .uri(llmServiceUrl))
            .build();
    }
}

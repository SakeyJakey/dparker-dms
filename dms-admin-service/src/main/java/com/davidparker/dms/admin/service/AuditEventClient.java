package com.davidparker.dms.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Service
public class AuditEventClient {

    private final WebClient webClient;

    public AuditEventClient(@Value("${dms.services.audit-service-url}") String auditServiceUrl) {
        this.webClient = WebClient.builder()
            .baseUrl(auditServiceUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    public void logEvent(Map<String, Object> auditEvent) {
        webClient.post()
            .uri("/api/v1/audit/events")
            .bodyValue(auditEvent)
            .retrieve()
            .bodyToMono(Void.class)
            .onErrorResume(e -> {
                // Log error but don't fail the operation
                System.err.println("Failed to log audit event: " + e.getMessage());
                return Mono.empty();
            })
            .subscribe();
    }
}

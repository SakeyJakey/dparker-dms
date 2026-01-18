package com.davidparker.dms.core.client;

import com.davidparker.dms.core.dto.AuditEventDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Shared client for sending audit events to the audit service.
 * Can be used by all DMS services to log audit events.
 */
@Component
public class AuditEventClient {

    private final WebClient webClient;
    private final String auditServiceUrl;
    private final ObjectMapper objectMapper;

    public AuditEventClient(@Value("${dms.services.audit-service-url:http://dms-audit-service:8082}") String auditServiceUrl) {
        this.auditServiceUrl = auditServiceUrl;
        this.objectMapper = new ObjectMapper();
        this.webClient = WebClient.builder()
            .baseUrl(auditServiceUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    /**
     * Logs an audit event to the audit service.
     * 
     * @param eventData Map containing audit event data
     */
    public void logEvent(Map<String, Object> eventData) {
        webClient.post()
            .uri("/api/v1/audit/events")
            .bodyValue(eventData)
            .retrieve()
            .bodyToMono(Void.class)
            .onErrorResume(e -> {
                // Log error but don't fail the operation
                System.err.println("Failed to log audit event: " + e.getMessage());
                return Mono.empty();
            })
            .subscribe();
    }

    /**
     * Logs an audit event using the DTO.
     * 
     * @param auditEvent Audit event DTO
     */
    public void logEvent(AuditEventDto auditEvent) {
        try {
            // Convert DTO to map using Jackson
            @SuppressWarnings("unchecked")
            Map<String, Object> eventDataMap = objectMapper.convertValue(auditEvent, Map.class);
            // Ensure timestamp is set if null
            if (!eventDataMap.containsKey("timestamp") || eventDataMap.get("timestamp") == null) {
                eventDataMap.put("timestamp", java.time.Instant.now().toString());
            }
            logEvent(eventDataMap);
        } catch (Exception e) {
            // Log error but don't fail the operation
            System.err.println("Failed to convert audit event DTO to map: " + e.getMessage());
        }
    }
}

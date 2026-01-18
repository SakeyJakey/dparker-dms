package com.davidparker.dms.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Shared DTO for audit events that can be sent to the audit service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEventDto {
    private String eventType;
    private String eventCategory;
    private String action;
    private String result;
    private Instant timestamp;
    private UUID userId;
    private String username;
    private UUID applicationId;
    private String applicationName;
    private String ipAddress;
    private String userAgent;
    private UUID requestId;
    private UUID correlationId;
    private String resourceType;
    private UUID resourceId;
    private String resourceName;
    private Map<String, Object> details;
    private Map<String, Object> previousState;
    private Map<String, Object> newState;
    private Boolean pciRelevant;
    private Boolean gdprRelevant;
    private Boolean containsPii;
}

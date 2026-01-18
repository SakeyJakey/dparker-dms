package com.davidparker.dms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_id", nullable = false, length = 50)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Column(name = "event_category", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private EventCategory eventCategory;

    @Column(nullable = false)
    private Instant timestamp;

    // Actor Information
    @Column(name = "user_id")
    private UUID userId;

    @Column(length = 255)
    private String username;

    @Column(name = "user_roles", columnDefinition = "JSONB")
    @Convert(converter = JsonConverter.class)
    private Map<String, Object> userRoles;

    @Column(name = "application_id")
    private UUID applicationId;

    @Column(name = "application_name", length = 255)
    private String applicationName;

    // Request Context
    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "request_id")
    private UUID requestId;

    @Column(name = "correlation_id")
    private UUID correlationId;

    // Resource Information
    @Column(name = "resource_type", length = 50)
    private String resourceType;

    @Column(name = "resource_id")
    private UUID resourceId;

    @Column(name = "resource_name", length = 500)
    private String resourceName;

    // Event Details
    @Column(nullable = false, length = 50)
    private String action;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AuditResult result;

    @Column(columnDefinition = "JSONB")
    @Convert(converter = JsonConverter.class)
    private Map<String, Object> details;

    @Column(name = "previous_state", columnDefinition = "JSONB")
    @Convert(converter = JsonConverter.class)
    private Map<String, Object> previousState;

    @Column(name = "new_state", columnDefinition = "JSONB")
    @Convert(converter = JsonConverter.class)
    private Map<String, Object> newState;

    // Compliance Markers
    @Column(name = "pci_relevant")
    @Builder.Default
    private Boolean pciRelevant = false;

    @Column(name = "gdpr_relevant")
    @Builder.Default
    private Boolean gdprRelevant = false;

    @Column(name = "contains_pii")
    @Builder.Default
    private Boolean containsPii = false;

    // Integrity
    @Column(nullable = false, length = 64)
    private String checksum;

    public enum EventType {
        LOGIN, LOGOUT, FAILED_LOGIN, TOKEN_REFRESH,
        VIEW, DOWNLOAD, PRINT, SHARE,
        CREATE, UPDATE, DELETE, ARCHIVE,
        GRANT, REVOKE, ROLE_ASSIGN,
        QUERY_INITIATED, QUERY_COMPLETED, CONTENT_FILTERED,
        USER_CREATE, CONFIG_CHANGE, APP_PROVISION,
        EXPORT_REQUEST, DELETION_REQUEST, CONSENT_CHANGE,
        LLM_QUERY_INITIATED, LLM_QUERY_COMPLETED, LLM_INTERACTION,
        DOCUMENT_INDEXED_FOR_LLM, GDPR_ERASURE_COMPLETED, GDPR_DATA_EXPORT
    }

    public enum EventCategory {
        AUTHENTICATION, DOCUMENT_ACCESS, DOCUMENT_LIFECYCLE,
        PERMISSION_CHANGES, LLM_QUERIES, ADMIN_ACTIONS, DATA_SUBJECT, PCI_ACCESS
    }

    public enum AuditResult {
        SUCCESS, FAILURE, PARTIAL
    }
}

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
@Table(name = "registered_applications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisteredApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String entraAppId;  // Azure AD App Registration ID

    @Column(unique = true, nullable = false)
    private String applicationName;

    @Column(nullable = false)
    private String storageContainerName;

    @Column(nullable = false)
    private String encryptionKeyName;  // Key Vault key reference

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    @Column(columnDefinition = "jsonb")
    @Convert(converter = JsonConverter.class)
    private Map<String, Object> configuration;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public enum ApplicationStatus {
        ACTIVE, INACTIVE, SUSPENDED
    }
}

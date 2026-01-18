package com.davidparker.dms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private RegisteredApplication application;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private Classification classification;

    @Column(name = "pci_relevant")
    @Builder.Default
    private Boolean pciRelevant = false;

    @Column(name = "gdpr_data_categories", columnDefinition = "TEXT[]")
    @ElementCollection
    private List<String> gdprDataCategories;

    @Column(name = "retention_until")
    private Instant retentionUntil;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public enum Classification {
        PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED, PCI
    }
}

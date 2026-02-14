package com.davidparker.dms.document.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "document_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private Document.Classification defaultClassification;

    @Column(name = "content_template", columnDefinition = "TEXT")
    private String contentTemplate;

    @Column(name = "metadata_schema", columnDefinition = "TEXT")
    private String metadataSchema;

    @Column(name = "application_id")
    private UUID applicationId;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = Instant.now(); updatedAt = Instant.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }
}

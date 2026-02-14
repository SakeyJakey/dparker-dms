package com.davidparker.dms.document.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "document_workflows")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentWorkflow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private WorkflowStatus status;

    @Column(name = "previous_status", length = 30)
    @Enumerated(EnumType.STRING)
    private WorkflowStatus previousStatus;

    @Column(name = "assigned_to")
    private UUID assignedTo;

    @Column(name = "assigned_by")
    private UUID assignedBy;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(name = "due_date")
    private Instant dueDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = Instant.now(); updatedAt = Instant.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }

    public enum WorkflowStatus {
        DRAFT, REVIEW, APPROVED, REJECTED, PUBLISHED, ARCHIVED;

        public boolean canTransitionTo(WorkflowStatus target) {
            return switch (this) {
                case DRAFT -> target == REVIEW || target == ARCHIVED;
                case REVIEW -> target == APPROVED || target == REJECTED;
                case APPROVED -> target == PUBLISHED || target == ARCHIVED;
                case REJECTED -> target == DRAFT || target == ARCHIVED;
                case PUBLISHED -> target == ARCHIVED;
                case ARCHIVED -> false;
            };
        }
    }
}

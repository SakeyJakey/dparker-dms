package com.davidparker.dms.document.service;

import com.davidparker.dms.document.dto.WorkflowTransitionRequest;
import com.davidparker.dms.document.model.DocumentWorkflow;
import com.davidparker.dms.document.model.DocumentWorkflow.WorkflowStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WorkflowService {

    private final AuditEventClient auditEventClient;
    private final Map<UUID, List<DocumentWorkflow>> workflowStore = new ConcurrentHashMap<>();

    public WorkflowService(AuditEventClient auditEventClient) {
        this.auditEventClient = auditEventClient;
    }

    public DocumentWorkflow getCurrentWorkflow(UUID documentId) {
        List<DocumentWorkflow> history = workflowStore.getOrDefault(documentId, new ArrayList<>());
        if (history.isEmpty()) {
            DocumentWorkflow initial = DocumentWorkflow.builder()
                .id(UUID.randomUUID())
                .documentId(documentId)
                .status(WorkflowStatus.DRAFT)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
            history.add(initial);
            workflowStore.put(documentId, history);
            return initial;
        }
        return history.get(history.size() - 1);
    }

    public List<DocumentWorkflow> getWorkflowHistory(UUID documentId) {
        return workflowStore.getOrDefault(documentId, new ArrayList<>());
    }

    public DocumentWorkflow transition(UUID documentId, WorkflowTransitionRequest request) {
        DocumentWorkflow current = getCurrentWorkflow(documentId);
        WorkflowStatus targetStatus = request.getTargetStatus();

        if (!current.getStatus().canTransitionTo(targetStatus)) {
            throw new IllegalStateException(
                "Cannot transition from " + current.getStatus() + " to " + targetStatus);
        }

        DocumentWorkflow newWorkflow = DocumentWorkflow.builder()
            .id(UUID.randomUUID())
            .documentId(documentId)
            .status(targetStatus)
            .previousStatus(current.getStatus())
            .assignedTo(request.getAssignedTo())
            .comments(request.getComments())
            .dueDate(request.getDueDate())
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        workflowStore.computeIfAbsent(documentId, k -> new ArrayList<>()).add(newWorkflow);

        Map<String, Object> auditEvent = Map.of(
            "eventType", "UPDATE",
            "eventCategory", "DOCUMENT_LIFECYCLE",
            "action", "WORKFLOW_TRANSITION",
            "result", "SUCCESS",
            "resourceType", "Document",
            "resourceId", documentId.toString(),
            "details", Map.of("from", current.getStatus().name(), "to", targetStatus.name()),
            "timestamp", Instant.now().toString()
        );
        auditEventClient.logEvent(new java.util.HashMap<>(auditEvent));

        return newWorkflow;
    }
}

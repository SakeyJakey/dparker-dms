package com.davidparker.dms.document.dto;

import com.davidparker.dms.document.model.DocumentWorkflow;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class WorkflowTransitionRequest {
    @NotNull
    private DocumentWorkflow.WorkflowStatus targetStatus;
    private UUID assignedTo;
    private String comments;
    private Instant dueDate;
}

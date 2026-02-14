package com.davidparker.dms.document.controller;

import com.davidparker.dms.document.dto.WorkflowTransitionRequest;
import com.davidparker.dms.document.model.DocumentWorkflow;
import com.davidparker.dms.document.service.WorkflowService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents/{documentId}/workflow")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping
    public ResponseEntity<DocumentWorkflow> getCurrentWorkflow(@PathVariable UUID documentId) {
        return ResponseEntity.ok(workflowService.getCurrentWorkflow(documentId));
    }

    @GetMapping("/history")
    public ResponseEntity<List<DocumentWorkflow>> getWorkflowHistory(@PathVariable UUID documentId) {
        return ResponseEntity.ok(workflowService.getWorkflowHistory(documentId));
    }

    @PostMapping("/transition")
    public ResponseEntity<DocumentWorkflow> transitionWorkflow(
            @PathVariable UUID documentId,
            @Valid @RequestBody WorkflowTransitionRequest request) {
        return ResponseEntity.ok(workflowService.transition(documentId, request));
    }

    @PostMapping("/submit-for-review")
    public ResponseEntity<DocumentWorkflow> submitForReview(
            @PathVariable UUID documentId,
            @RequestParam(required = false) UUID reviewerId,
            @RequestParam(required = false) String comments) {
        WorkflowTransitionRequest req = new WorkflowTransitionRequest();
        req.setTargetStatus(DocumentWorkflow.WorkflowStatus.REVIEW);
        req.setAssignedTo(reviewerId);
        req.setComments(comments);
        return ResponseEntity.ok(workflowService.transition(documentId, req));
    }

    @PostMapping("/approve")
    public ResponseEntity<DocumentWorkflow> approve(
            @PathVariable UUID documentId,
            @RequestParam(required = false) String comments) {
        WorkflowTransitionRequest req = new WorkflowTransitionRequest();
        req.setTargetStatus(DocumentWorkflow.WorkflowStatus.APPROVED);
        req.setComments(comments);
        return ResponseEntity.ok(workflowService.transition(documentId, req));
    }

    @PostMapping("/reject")
    public ResponseEntity<DocumentWorkflow> reject(
            @PathVariable UUID documentId,
            @RequestParam(required = false) String comments) {
        WorkflowTransitionRequest req = new WorkflowTransitionRequest();
        req.setTargetStatus(DocumentWorkflow.WorkflowStatus.REJECTED);
        req.setComments(comments);
        return ResponseEntity.ok(workflowService.transition(documentId, req));
    }

    @PostMapping("/publish")
    public ResponseEntity<DocumentWorkflow> publish(@PathVariable UUID documentId) {
        WorkflowTransitionRequest req = new WorkflowTransitionRequest();
        req.setTargetStatus(DocumentWorkflow.WorkflowStatus.PUBLISHED);
        return ResponseEntity.ok(workflowService.transition(documentId, req));
    }

    @PostMapping("/archive")
    public ResponseEntity<DocumentWorkflow> archive(@PathVariable UUID documentId) {
        WorkflowTransitionRequest req = new WorkflowTransitionRequest();
        req.setTargetStatus(DocumentWorkflow.WorkflowStatus.ARCHIVED);
        return ResponseEntity.ok(workflowService.transition(documentId, req));
    }
}

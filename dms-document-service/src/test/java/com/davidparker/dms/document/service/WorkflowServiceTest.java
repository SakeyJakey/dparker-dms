package com.davidparker.dms.document.service;

import com.davidparker.dms.document.dto.WorkflowTransitionRequest;
import com.davidparker.dms.document.model.DocumentWorkflow;
import com.davidparker.dms.document.model.DocumentWorkflow.WorkflowStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {

    @Mock
    private AuditEventClient auditEventClient;

    private WorkflowService workflowService;
    private UUID documentId;

    @BeforeEach
    void setUp() {
        workflowService = new WorkflowService(auditEventClient);
        documentId = UUID.randomUUID();
        lenient().doNothing().when(auditEventClient).logEvent(any());
    }

    @Test
    void testGetCurrentWorkflowReturnsDefaultDraft() {
        DocumentWorkflow workflow = workflowService.getCurrentWorkflow(documentId);
        assertNotNull(workflow);
        assertEquals(WorkflowStatus.DRAFT, workflow.getStatus());
        assertEquals(documentId, workflow.getDocumentId());
    }

    @Test
    void testTransitionDraftToReview() {
        workflowService.getCurrentWorkflow(documentId);
        WorkflowTransitionRequest request = new WorkflowTransitionRequest();
        request.setTargetStatus(WorkflowStatus.REVIEW);
        request.setComments("Please review");

        DocumentWorkflow result = workflowService.transition(documentId, request);

        assertEquals(WorkflowStatus.REVIEW, result.getStatus());
        assertEquals(WorkflowStatus.DRAFT, result.getPreviousStatus());
        assertEquals("Please review", result.getComments());
    }

    @Test
    void testTransitionReviewToApproved() {
        workflowService.getCurrentWorkflow(documentId);
        WorkflowTransitionRequest toReview = new WorkflowTransitionRequest();
        toReview.setTargetStatus(WorkflowStatus.REVIEW);
        workflowService.transition(documentId, toReview);

        WorkflowTransitionRequest toApproved = new WorkflowTransitionRequest();
        toApproved.setTargetStatus(WorkflowStatus.APPROVED);
        DocumentWorkflow result = workflowService.transition(documentId, toApproved);

        assertEquals(WorkflowStatus.APPROVED, result.getStatus());
        assertEquals(WorkflowStatus.REVIEW, result.getPreviousStatus());
    }

    @Test
    void testTransitionReviewToRejected() {
        workflowService.getCurrentWorkflow(documentId);
        WorkflowTransitionRequest toReview = new WorkflowTransitionRequest();
        toReview.setTargetStatus(WorkflowStatus.REVIEW);
        workflowService.transition(documentId, toReview);

        WorkflowTransitionRequest toRejected = new WorkflowTransitionRequest();
        toRejected.setTargetStatus(WorkflowStatus.REJECTED);
        toRejected.setComments("Needs revisions");
        DocumentWorkflow result = workflowService.transition(documentId, toRejected);

        assertEquals(WorkflowStatus.REJECTED, result.getStatus());
    }

    @Test
    void testInvalidTransitionThrowsException() {
        workflowService.getCurrentWorkflow(documentId);
        WorkflowTransitionRequest request = new WorkflowTransitionRequest();
        request.setTargetStatus(WorkflowStatus.PUBLISHED);

        assertThrows(IllegalStateException.class, () ->
            workflowService.transition(documentId, request));
    }

    @Test
    void testGetWorkflowHistory() {
        workflowService.getCurrentWorkflow(documentId);
        WorkflowTransitionRequest toReview = new WorkflowTransitionRequest();
        toReview.setTargetStatus(WorkflowStatus.REVIEW);
        workflowService.transition(documentId, toReview);

        List<DocumentWorkflow> history = workflowService.getWorkflowHistory(documentId);
        assertEquals(2, history.size());
        assertEquals(WorkflowStatus.DRAFT, history.get(0).getStatus());
        assertEquals(WorkflowStatus.REVIEW, history.get(1).getStatus());
    }

    @Test
    void testWorkflowStatusTransitionValidation() {
        assertTrue(WorkflowStatus.DRAFT.canTransitionTo(WorkflowStatus.REVIEW));
        assertTrue(WorkflowStatus.DRAFT.canTransitionTo(WorkflowStatus.ARCHIVED));
        assertFalse(WorkflowStatus.DRAFT.canTransitionTo(WorkflowStatus.PUBLISHED));
        assertFalse(WorkflowStatus.DRAFT.canTransitionTo(WorkflowStatus.APPROVED));

        assertTrue(WorkflowStatus.REVIEW.canTransitionTo(WorkflowStatus.APPROVED));
        assertTrue(WorkflowStatus.REVIEW.canTransitionTo(WorkflowStatus.REJECTED));
        assertFalse(WorkflowStatus.REVIEW.canTransitionTo(WorkflowStatus.PUBLISHED));

        assertTrue(WorkflowStatus.APPROVED.canTransitionTo(WorkflowStatus.PUBLISHED));
        assertTrue(WorkflowStatus.APPROVED.canTransitionTo(WorkflowStatus.ARCHIVED));

        assertFalse(WorkflowStatus.ARCHIVED.canTransitionTo(WorkflowStatus.DRAFT));
    }
}

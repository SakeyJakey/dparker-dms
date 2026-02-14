package com.davidparker.dms.document.service;

import com.davidparker.dms.document.dto.BulkOperationRequest;
import com.davidparker.dms.document.dto.BulkOperationResponse;
import com.davidparker.dms.document.model.Document;
import com.davidparker.dms.document.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulkOperationsServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private AuditEventClient auditEventClient;

    private BulkOperationsService bulkOperationsService;

    @BeforeEach
    void setUp() {
        bulkOperationsService = new BulkOperationsService(documentRepository, auditEventClient);
        doNothing().when(auditEventClient).logEvent(any());
    }

    @Test
    void testBulkDelete() {
        UUID docId1 = UUID.randomUUID();
        UUID docId2 = UUID.randomUUID();
        Document doc1 = Document.builder().id(docId1).name("doc1.pdf").build();
        Document doc2 = Document.builder().id(docId2).name("doc2.pdf").build();

        when(documentRepository.findById(docId1)).thenReturn(Optional.of(doc1));
        when(documentRepository.findById(docId2)).thenReturn(Optional.of(doc2));

        BulkOperationRequest request = new BulkOperationRequest();
        request.setDocumentIds(List.of(docId1, docId2));
        request.setAction(BulkOperationRequest.BulkAction.DELETE);

        BulkOperationResponse response = bulkOperationsService.executeBulkOperation(request);

        assertEquals(2, response.getTotalRequested());
        assertEquals(2, response.getSuccessCount());
        assertEquals(0, response.getFailureCount());
        verify(documentRepository, times(2)).delete(any());
    }

    @Test
    void testBulkClassify() {
        UUID docId = UUID.randomUUID();
        Document doc = Document.builder().id(docId).name("doc.pdf").classification(Document.Classification.PUBLIC).build();
        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(documentRepository.save(any())).thenReturn(doc);

        BulkOperationRequest request = new BulkOperationRequest();
        request.setDocumentIds(List.of(docId));
        request.setAction(BulkOperationRequest.BulkAction.CLASSIFY);
        request.setTargetClassification(Document.Classification.CONFIDENTIAL);

        BulkOperationResponse response = bulkOperationsService.executeBulkOperation(request);

        assertEquals(1, response.getSuccessCount());
        assertEquals(Document.Classification.CONFIDENTIAL, doc.getClassification());
    }

    @Test
    void testBulkOperationWithNotFound() {
        UUID docId = UUID.randomUUID();
        when(documentRepository.findById(docId)).thenReturn(Optional.empty());

        BulkOperationRequest request = new BulkOperationRequest();
        request.setDocumentIds(List.of(docId));
        request.setAction(BulkOperationRequest.BulkAction.DELETE);

        BulkOperationResponse response = bulkOperationsService.executeBulkOperation(request);

        assertEquals(1, response.getTotalRequested());
        assertEquals(0, response.getSuccessCount());
        assertEquals(1, response.getFailureCount());
    }
}

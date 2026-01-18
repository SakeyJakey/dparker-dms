package com.davidparker.dms.dms_document_service.service;

import com.davidparker.dms.dms_document_service.dto.DocumentVersionResponse;
import com.davidparker.dms.model.Document;
import com.davidparker.dms.repository.DocumentRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentVersionService {

    private final DocumentRepository documentRepository;

    public DocumentVersionService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @PreAuthorize("hasAnyRole('DMS.davidparker-lv-bmth', 'DMS.User')")
    public List<DocumentVersionResponse> getDocumentVersions(UUID documentId) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document not found"));

        // TODO: Implement actual version tracking
        // For now, return current version
        List<DocumentVersionResponse> versions = new ArrayList<>();
        DocumentVersionResponse version = new DocumentVersionResponse();
        version.setVersion(1);
        version.setDocumentId(documentId);
        version.setCreatedAt(document.getCreatedAt());
        version.setCreatedBy(document.getCreatedBy());
        versions.add(version);
        
        return versions;
    }

    @PreAuthorize("hasAnyRole('DMS.davidparker-lv-bmth', 'DMS.User')")
    public DocumentVersionResponse getDocumentVersion(UUID documentId, Integer version) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document not found"));

        // TODO: Implement actual version retrieval
        DocumentVersionResponse versionResponse = new DocumentVersionResponse();
        versionResponse.setVersion(version);
        versionResponse.setDocumentId(documentId);
        versionResponse.setCreatedAt(document.getCreatedAt());
        versionResponse.setCreatedBy(document.getCreatedBy());
        
        return versionResponse;
    }
}

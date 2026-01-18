package com.davidparker.dms.document.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.davidparker.dms.document.model.Document;
import com.davidparker.dms.document.repository.DocumentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final BlobServiceClient blobServiceClient;
    private final AuditEventClient auditEventClient;
    private final ApplicationServiceClient applicationServiceClient;

    public DocumentService(
            DocumentRepository documentRepository,
            BlobServiceClient blobServiceClient,
            AuditEventClient auditEventClient,
            ApplicationServiceClient applicationServiceClient) {
        this.documentRepository = documentRepository;
        this.blobServiceClient = blobServiceClient;
        this.auditEventClient = auditEventClient;
        this.applicationServiceClient = applicationServiceClient;
    }

    @PreAuthorize("hasAnyRole('DMS.davidparker-lv-bmth', 'DMS.User')")
    public Page<Document> listDocuments(UUID applicationId, Document.Classification classification, Pageable pageable) {
        if (classification != null) {
            return documentRepository.findByApplicationIdAndClassification(applicationId, classification, pageable);
        }
        return documentRepository.findByApplicationId(applicationId, pageable);
    }

    @PreAuthorize("hasAnyRole('DMS.davidparker-lv-bmth', 'DMS.User')")
    @Transactional
    public Document uploadDocument(UUID applicationId, MultipartFile file, String name, Document.Classification classification) {
        // Verify application exists
        applicationServiceClient.getApplication(applicationId)
            .blockOptional()
            .orElseThrow(() -> new RuntimeException("Application not found"));

        Document document = Document.builder()
            .applicationId(applicationId)
            .name(name)
            .classification(classification)
            .pciRelevant(false)
            .version(1)
            .build();

        document = documentRepository.save(document);

        try {
            // Get container for application
            String containerName = "davidparker-lv-bmth-documents";
            BlobContainerClient container = blobServiceClient.getBlobContainerClient(containerName);
            
            String blobPath = buildBlobPath(document.getId());
            BlobClient blobClient = container.getBlobClient(blobPath);
            blobClient.upload(file.getInputStream(), file.getSize(), true);
            
            document.setBlobUrl(blobClient.getBlobUrl());
            document = documentRepository.save(document);
            
            // Log audit event
            Map<String, Object> auditEvent = new HashMap<>();
            auditEvent.put("eventType", "CREATE");
            auditEvent.put("eventCategory", "DOCUMENT_LIFECYCLE");
            auditEvent.put("action", "DOCUMENT_UPLOADED");
            auditEvent.put("result", "SUCCESS");
            auditEvent.put("resourceType", "Document");
            auditEvent.put("resourceId", document.getId().toString());
            auditEvent.put("resourceName", name);
            auditEvent.put("applicationId", applicationId.toString());
            auditEvent.put("timestamp", Instant.now().toString());
            auditEventClient.logEvent(auditEvent);

            // Publish event for LLM service (async)
            publishDocumentUploadedEvent(document);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload document", e);
        }

        return document;
    }

    @PreAuthorize("hasAnyRole('DMS.davidparker-lv-bmth', 'DMS.User')")
    public Document getDocument(UUID id, UUID applicationId) {
        Document document = documentRepository.findByIdAndApplicationId(id, applicationId)
            .stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Document not found"));

        // Log audit event
        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("eventType", "VIEW");
        auditEvent.put("eventCategory", "DOCUMENT_ACCESS");
        auditEvent.put("action", "DOCUMENT_VIEWED");
        auditEvent.put("result", "SUCCESS");
        auditEvent.put("resourceType", "Document");
        auditEvent.put("resourceId", id.toString());
        auditEvent.put("applicationId", applicationId.toString());
        auditEvent.put("timestamp", Instant.now().toString());
        auditEventClient.logEvent(auditEvent);

        return document;
    }

    @PreAuthorize("hasAnyRole('DMS.davidparker-lv-bmth', 'DMS.User')")
    @Transactional
    public Document updateDocument(UUID id, UUID applicationId, Document updatedDocument) {
        Document document = documentRepository.findByIdAndApplicationId(id, applicationId)
            .stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Document not found"));

        document.setName(updatedDocument.getName());
        document.setClassification(updatedDocument.getClassification());
        document.setPciRelevant(updatedDocument.getPciRelevant());
        document.setGdprDataCategories(updatedDocument.getGdprDataCategories());

        document = documentRepository.save(document);

        // Log audit event
        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("eventType", "UPDATE");
        auditEvent.put("eventCategory", "DOCUMENT_LIFECYCLE");
        auditEvent.put("action", "DOCUMENT_UPDATED");
        auditEvent.put("result", "SUCCESS");
        auditEvent.put("resourceType", "Document");
        auditEvent.put("resourceId", id.toString());
        auditEvent.put("applicationId", applicationId.toString());
        auditEvent.put("timestamp", Instant.now().toString());
        auditEventClient.logEvent(auditEvent);

        return document;
    }

    @PreAuthorize("hasAnyRole('DMS.davidparker-lv-bmth', 'DMS.User')")
    @Transactional
    public void deleteDocument(UUID id, UUID applicationId) {
        Document document = documentRepository.findByIdAndApplicationId(id, applicationId)
            .stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Document not found"));

        documentRepository.delete(document);

        // Log audit event
        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("eventType", "DELETE");
        auditEvent.put("eventCategory", "DOCUMENT_LIFECYCLE");
        auditEvent.put("action", "DOCUMENT_DELETED");
        auditEvent.put("result", "SUCCESS");
        auditEvent.put("resourceType", "Document");
        auditEvent.put("resourceId", id.toString());
        auditEvent.put("applicationId", applicationId.toString());
        auditEvent.put("timestamp", Instant.now().toString());
        auditEventClient.logEvent(auditEvent);
    }

    @PreAuthorize("hasAnyRole('DMS.davidparker-lv-bmth', 'DMS.User')")
    public byte[] downloadDocument(UUID id, UUID applicationId) {
        Document document = documentRepository.findByIdAndApplicationId(id, applicationId)
            .stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Document not found"));

        // Log audit event
        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("eventType", "DOWNLOAD");
        auditEvent.put("eventCategory", "DOCUMENT_ACCESS");
        auditEvent.put("action", "DOCUMENT_DOWNLOADED");
        auditEvent.put("result", "SUCCESS");
        auditEvent.put("resourceType", "Document");
        auditEvent.put("resourceId", id.toString());
        auditEvent.put("applicationId", applicationId.toString());
        auditEvent.put("timestamp", Instant.now().toString());
        auditEventClient.logEvent(auditEvent);

        // Download from blob storage
        try {
            String containerName = "davidparker-lv-bmth-documents";
            BlobContainerClient container = blobServiceClient.getBlobContainerClient(containerName);
            String blobPath = buildBlobPath(id);
            BlobClient blobClient = container.getBlobClient(blobPath);
            return blobClient.downloadContent().toBytes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to download document", e);
        }
    }

    private String buildBlobPath(UUID documentId) {
        java.time.LocalDate now = java.time.LocalDate.now();
        return String.format("%d/%02d/%s/v1/content", 
            now.getYear(), 
            now.getMonthValue(), 
            documentId.toString());
    }

    private void publishDocumentUploadedEvent(Document document) {
        // Publish to LLM service for embedding (async via HTTP)
        // This would be done via message queue or HTTP call to LLM service
    }
}

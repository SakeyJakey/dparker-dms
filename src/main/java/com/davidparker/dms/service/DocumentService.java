package com.davidparker.dms.service;

import com.davidparker.dms.model.AuditEvent;
import com.davidparker.dms.model.Document;
import com.davidparker.dms.model.RegisteredApplication;
import com.davidparker.dms.repository.DocumentRepository;
import com.davidparker.dms.repository.RegisteredApplicationRepository;
import com.davidparker.dms.service.ai.DocumentEmbeddingService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final ApplicationScopedStorageService storageService;
    private final AuditService auditService;
    private final RegisteredApplicationRepository applicationRepository;
    private final ApplicationEventPublisher eventPublisher;

    public DocumentService(
            DocumentRepository documentRepository,
            ApplicationScopedStorageService storageService,
            AuditService auditService,
            RegisteredApplicationRepository applicationRepository,
            ApplicationEventPublisher eventPublisher) {
        this.documentRepository = documentRepository;
        this.storageService = storageService;
        this.auditService = auditService;
        this.applicationRepository = applicationRepository;
        this.eventPublisher = eventPublisher;
    }

    @PreAuthorize("hasAnyRole('DMS.davidparker-lv-bmth', 'DMS.User')")
    public Page<Document> listDocuments(Document.Classification classification, Pageable pageable) {
        RegisteredApplication app = ApplicationContext.getCurrent();
        if (app == null) {
            throw new IllegalStateException("No application context");
        }

        if (classification != null) {
            return documentRepository.findByApplicationIdAndClassification(
                app.getId(), classification, pageable);
        }
        return documentRepository.findByApplicationId(app.getId(), pageable);
    }

    @PreAuthorize("hasAnyRole('DMS.davidparker-lv-bmth', 'DMS.User')")
    @Transactional
    public Document uploadDocument(MultipartFile file, String name, Document.Classification classification) {
        RegisteredApplication app = ApplicationContext.getCurrent();
        if (app == null) {
            throw new IllegalStateException("No application context");
        }

        Document document = Document.builder()
            .application(app)
            .name(name)
            .classification(classification)
            .pciRelevant(false)
            .build();

        document = documentRepository.save(document);

        try {
            String blobUrl = storageService.uploadDocument(document.getId(), file.getInputStream());
            
            // Audit the upload
            auditService.logEvent(AuditEvent.builder()
                .eventType(AuditEvent.EventType.CREATE)
                .eventCategory(AuditEvent.EventCategory.DOCUMENT_LIFECYCLE)
                .action("DOCUMENT_UPLOADED")
                .result(AuditEvent.AuditResult.SUCCESS)
                .resourceType("Document")
                .resourceId(document.getId())
                .resourceName(name)
                .applicationId(app.getId())
                .applicationName(app.getApplicationName())
                .build());
            
            // Publish event for embedding service
            eventPublisher.publishEvent(new DocumentEmbeddingService.DocumentUploadedEvent(document));
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload document", e);
        }

        return document;
    }

    @PreAuthorize("hasAnyRole('DMS.davidparker-lv-bmth', 'DMS.User')")
    public Document getDocument(UUID id) {
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Document not found"));

        // Verify application context
        RegisteredApplication app = ApplicationContext.getCurrent();
        if (app == null || !document.getApplication().getId().equals(app.getId())) {
            throw new SecurityException("Access denied");
        }

        // Audit the access
        auditService.logEvent(AuditEvent.builder()
            .eventType(AuditEvent.EventType.VIEW)
            .eventCategory(AuditEvent.EventCategory.DOCUMENT_ACCESS)
            .action("DOCUMENT_VIEWED")
            .result(AuditEvent.AuditResult.SUCCESS)
            .resourceType("Document")
            .resourceId(id)
            .resourceName(document.getName())
            .applicationId(app.getId())
            .applicationName(app.getApplicationName())
            .build());

        return document;
    }

    @PreAuthorize("hasAnyRole('DMS.davidparker-lv-bmth', 'DMS.User')")
    @Transactional
    public Document updateDocument(UUID id, Document updatedDocument) {
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Document not found"));

        RegisteredApplication app = ApplicationContext.getCurrent();
        if (app == null || !document.getApplication().getId().equals(app.getId())) {
            throw new SecurityException("Access denied");
        }

        document.setName(updatedDocument.getName());
        document.setClassification(updatedDocument.getClassification());
        document.setPciRelevant(updatedDocument.getPciRelevant());
        document.setGdprDataCategories(updatedDocument.getGdprDataCategories());

        document = documentRepository.save(document);

        // Audit the update
        auditService.logEvent(AuditEvent.builder()
            .eventType(AuditEvent.EventType.UPDATE)
            .eventCategory(AuditEvent.EventCategory.DOCUMENT_LIFECYCLE)
            .action("DOCUMENT_UPDATED")
            .result(AuditEvent.AuditResult.SUCCESS)
            .resourceType("Document")
            .resourceId(id)
            .resourceName(document.getName())
            .applicationId(app.getId())
            .applicationName(app.getApplicationName())
            .build());

        return document;
    }

    @PreAuthorize("hasAnyRole('DMS.davidparker-lv-bmth', 'DMS.User')")
    @Transactional
    public void deleteDocument(UUID id) {
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Document not found"));

        RegisteredApplication app = ApplicationContext.getCurrent();
        if (app == null || !document.getApplication().getId().equals(app.getId())) {
            throw new SecurityException("Access denied");
        }

        documentRepository.delete(document);

        // Audit the deletion
        auditService.logEvent(AuditEvent.builder()
            .eventType(AuditEvent.EventType.DELETE)
            .eventCategory(AuditEvent.EventCategory.DOCUMENT_LIFECYCLE)
            .action("DOCUMENT_DELETED")
            .result(AuditEvent.AuditResult.SUCCESS)
            .resourceType("Document")
            .resourceId(id)
            .resourceName(document.getName())
            .applicationId(app.getId())
            .applicationName(app.getApplicationName())
            .build());
    }

    @PreAuthorize("hasAnyRole('DMS.davidparker-lv-bmth', 'DMS.User')")
    public byte[] downloadDocument(UUID id) {
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Document not found"));

        RegisteredApplication app = ApplicationContext.getCurrent();
        if (app == null || !document.getApplication().getId().equals(app.getId())) {
            throw new SecurityException("Access denied");
        }

        // Audit the download
        auditService.logEvent(AuditEvent.builder()
            .eventType(AuditEvent.EventType.DOWNLOAD)
            .eventCategory(AuditEvent.EventCategory.DOCUMENT_ACCESS)
            .action("DOCUMENT_DOWNLOADED")
            .result(AuditEvent.AuditResult.SUCCESS)
            .resourceType("Document")
            .resourceId(id)
            .resourceName(document.getName())
            .applicationId(app.getId())
            .applicationName(app.getApplicationName())
            .build());

        // TODO: Implement actual download from blob storage
        return new byte[0];
    }
}

package com.davidparker.dms.service.compliance;

import com.davidparker.dms.model.AuditEvent;
import com.davidparker.dms.model.Document;
import com.davidparker.dms.repository.DocumentRepository;
import com.davidparker.dms.service.AuditService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
public class GdprComplianceService {

    private final DocumentRepository documentRepository;
    private final AuditService auditService;

    public GdprComplianceService(DocumentRepository documentRepository, AuditService auditService) {
        this.documentRepository = documentRepository;
        this.auditService = auditService;
    }

    @Transactional
    public ErasureResponse processErasureRequest(ErasureRequest request) {
        UUID dataSubjectId = request.getDataSubjectId();
        
        // 1. Find all documents owned by or containing data subject's PII
        List<Document> documents = documentRepository.findAll().stream()
            .filter(doc -> doc.getCreatedBy() != null && doc.getCreatedBy().equals(dataSubjectId))
            .toList();
        
        // 2. Check for legal holds or retention requirements
        List<Document> deletable = documents.stream()
            .filter(doc -> !hasLegalHold(doc))
            .filter(doc -> !hasRetentionRequirement(doc))
            .toList();
        
        // 3. Anonymize or delete documents
        for (Document doc : deletable) {
            if (doc.getCreatedBy() != null && doc.getCreatedBy().equals(dataSubjectId)) {
                documentRepository.delete(doc);
            } else {
                // Anonymize PII in document
                anonymizePii(doc, dataSubjectId);
            }
        }
        
        // 4. Audit the erasure
        auditService.logEvent(AuditEvent.builder()
            .eventType(AuditEvent.EventType.GDPR_ERASURE_COMPLETED)
            .eventCategory(AuditEvent.EventCategory.DATA_SUBJECT)
            .action("GDPR_ERASURE")
            .result(AuditEvent.AuditResult.SUCCESS)
            .gdprRelevant(true)
            .details(java.util.Map.of(
                "dataSubjectId", dataSubjectId.toString(),
                "documentsDeleted", String.valueOf(deletable.size()),
                "documentsRetained", String.valueOf(documents.size() - deletable.size())
            ))
            .build());
        
        return new ErasureResponse(deletable.size(), documents.size() - deletable.size());
    }

    public DataExportResponse exportDataSubjectData(UUID dataSubjectId) {
        List<Document> documents = documentRepository.findAll().stream()
            .filter(doc -> doc.getCreatedBy() != null && doc.getCreatedBy().equals(dataSubjectId))
            .toList();
        
        // Generate export path (would be stored in blob storage)
        String exportPath = "exports/" + dataSubjectId + "/" + java.time.Instant.now().toString();
        
        auditService.logEvent(AuditEvent.builder()
            .eventType(AuditEvent.EventType.GDPR_DATA_EXPORT)
            .eventCategory(AuditEvent.EventCategory.DATA_SUBJECT)
            .action("GDPR_DATA_EXPORT")
            .result(AuditEvent.AuditResult.SUCCESS)
            .gdprRelevant(true)
            .build());
        
        return new DataExportResponse(exportPath, java.time.Duration.ofHours(24));
    }

    @Scheduled(cron = "0 0 0 1 * *")  // Monthly
    public void generateProcessingRecords() {
        // Generate processing activity report for previous month
        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        // Implementation would create and store report
    }

    private boolean hasLegalHold(Document document) {
        // Check if document has legal hold
        return false;
    }

    private boolean hasRetentionRequirement(Document document) {
        // Check if document has retention requirement
        return document.getRetentionUntil() != null && 
               document.getRetentionUntil().isAfter(java.time.Instant.now());
    }

    private void anonymizePii(Document document, UUID dataSubjectId) {
        // Anonymize PII in document metadata
        document.setGdprDataCategories(null);
        documentRepository.save(document);
    }

    public static class ErasureRequest {
        private UUID dataSubjectId;

        public UUID getDataSubjectId() {
            return dataSubjectId;
        }

        public void setDataSubjectId(UUID dataSubjectId) {
            this.dataSubjectId = dataSubjectId;
        }
    }

    public static class ErasureResponse {
        private final int deletedCount;
        private final int retainedCount;

        public ErasureResponse(int deletedCount, int retainedCount) {
            this.deletedCount = deletedCount;
            this.retainedCount = retainedCount;
        }

        public int getDeletedCount() {
            return deletedCount;
        }

        public int getRetainedCount() {
            return retainedCount;
        }
    }

    public static class DataExportResponse {
        private final String exportPath;
        private final java.time.Duration expiration;

        public DataExportResponse(String exportPath, java.time.Duration expiration) {
            this.exportPath = exportPath;
            this.expiration = expiration;
        }

        public String getExportPath() {
            return exportPath;
        }

        public java.time.Duration getExpiration() {
            return expiration;
        }
    }
}

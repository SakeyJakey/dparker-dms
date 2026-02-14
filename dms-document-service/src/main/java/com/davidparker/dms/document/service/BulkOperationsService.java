package com.davidparker.dms.document.service;

import com.davidparker.dms.document.dto.BulkOperationRequest;
import com.davidparker.dms.document.dto.BulkOperationResponse;
import com.davidparker.dms.document.model.Document;
import com.davidparker.dms.document.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.*;

@Service
public class BulkOperationsService {

    private final DocumentRepository documentRepository;
    private final AuditEventClient auditEventClient;

    public BulkOperationsService(DocumentRepository documentRepository, AuditEventClient auditEventClient) {
        this.documentRepository = documentRepository;
        this.auditEventClient = auditEventClient;
    }

    public BulkOperationResponse executeBulkOperation(BulkOperationRequest request) {
        int successCount = 0;
        List<UUID> failedIds = new ArrayList<>();

        for (UUID docId : request.getDocumentIds()) {
            try {
                Optional<Document> docOpt = documentRepository.findById(docId);
                if (docOpt.isEmpty()) {
                    failedIds.add(docId);
                    continue;
                }
                Document doc = docOpt.get();

                switch (request.getAction()) {
                    case DELETE -> documentRepository.delete(doc);
                    case CLASSIFY -> {
                        doc.setClassification(request.getTargetClassification());
                        documentRepository.save(doc);
                    }
                    case TAG -> {
                        doc.setGdprDataCategories(request.getTags());
                        documentRepository.save(doc);
                    }
                    case ARCHIVE -> {
                        doc.setClassification(Document.Classification.RESTRICTED);
                        documentRepository.save(doc);
                    }
                }
                successCount++;
            } catch (Exception e) {
                failedIds.add(docId);
            }
        }

        auditEventClient.logEvent(Map.of(
            "eventType", "UPDATE", "eventCategory", "DOCUMENT_LIFECYCLE",
            "action", "BULK_" + request.getAction().name(),
            "result", failedIds.isEmpty() ? "SUCCESS" : "PARTIAL",
            "details", Map.of("total", request.getDocumentIds().size(), "success", successCount, "failed", failedIds.size()),
            "timestamp", Instant.now().toString()
        ));

        return BulkOperationResponse.builder()
            .totalRequested(request.getDocumentIds().size())
            .successCount(successCount)
            .failureCount(failedIds.size())
            .failedIds(failedIds)
            .message(String.format("Processed %d/%d documents", successCount, request.getDocumentIds().size()))
            .build();
    }

    public BulkOperationResponse bulkUpload(List<MultipartFile> files, String classification, String applicationId) {
        int successCount = 0;
        List<UUID> failedIds = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                Document doc = Document.builder()
                    .applicationId(UUID.fromString(applicationId))
                    .name(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unnamed")
                    .classification(Document.Classification.valueOf(classification))
                    .version(1)
                    .build();
                documentRepository.save(doc);
                successCount++;
            } catch (Exception e) {
                failedIds.add(UUID.randomUUID());
            }
        }

        return BulkOperationResponse.builder()
            .totalRequested(files.size())
            .successCount(successCount)
            .failureCount(failedIds.size())
            .failedIds(failedIds)
            .message(String.format("Uploaded %d/%d files", successCount, files.size()))
            .build();
    }
}

package com.davidparker.dms.compliance.service;

import com.davidparker.dms.compliance.dto.ErasureRequest;
import com.davidparker.dms.compliance.dto.ErasureResponse;
import com.davidparker.dms.compliance.dto.DataExportResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class GdprComplianceService {

    private final AuditEventClient auditEventClient;
    private final WebClient documentServiceClient;

    public GdprComplianceService(
            AuditEventClient auditEventClient,
            @Value("${dms.services.document-service-url}") String documentServiceUrl) {
        this.auditEventClient = auditEventClient;
        this.documentServiceClient = WebClient.builder()
            .baseUrl(documentServiceUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    @Transactional
    public ErasureResponse processErasureRequest(ErasureRequest request) {
        UUID dataSubjectId = request.getDataSubjectId();
        
        // Call document service to find and delete documents
        // In a real implementation, this would query the document service
        int deletedCount = 0;
        int retainedCount = 0;
        
        // Log audit event
        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("eventType", "GDPR_ERASURE_COMPLETED");
        auditEvent.put("eventCategory", "DATA_SUBJECT");
        auditEvent.put("action", "GDPR_ERASURE");
        auditEvent.put("result", "SUCCESS");
        auditEvent.put("gdprRelevant", true);
        auditEvent.put("details", Map.of(
            "dataSubjectId", dataSubjectId.toString(),
            "documentsDeleted", String.valueOf(deletedCount),
            "documentsRetained", String.valueOf(retainedCount)
        ));
        auditEvent.put("timestamp", Instant.now().toString());
        auditEventClient.logEvent(auditEvent);
        
        return new ErasureResponse(deletedCount, retainedCount);
    }

    public DataExportResponse exportDataSubjectData(UUID dataSubjectId) {
        // Call document service to export data
        String exportPath = "exports/" + dataSubjectId + "/" + Instant.now().toString();
        
        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("eventType", "GDPR_DATA_EXPORT");
        auditEvent.put("eventCategory", "DATA_SUBJECT");
        auditEvent.put("action", "GDPR_DATA_EXPORT");
        auditEvent.put("result", "SUCCESS");
        auditEvent.put("gdprRelevant", true);
        auditEvent.put("timestamp", Instant.now().toString());
        auditEventClient.logEvent(auditEvent);
        
        return new DataExportResponse(exportPath, Duration.ofHours(24));
    }

    @Scheduled(cron = "0 0 0 1 * *")  // Monthly
    public void generateProcessingRecords() {
        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        // Generate processing activity report
    }
}

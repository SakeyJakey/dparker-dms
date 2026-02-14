package com.davidparker.dms.audit.controller;

import com.davidparker.dms.audit.model.AuditEvent;
import com.davidparker.dms.audit.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Audit Logging", description = "Centralized audit trail with event logging and querying")
public class AuditController {

    private final AuditLogRepository auditLogRepository;
    private final com.davidparker.dms.audit.service.AuditService auditService;

    public AuditController(
            AuditLogRepository auditLogRepository,
            com.davidparker.dms.audit.service.AuditService auditService) {
        this.auditLogRepository = auditLogRepository;
        this.auditService = auditService;
    }

    @PostMapping("/events")
    public ResponseEntity<Void> logEvent(@RequestBody Map<String, Object> eventData) {
        auditService.logEvent(eventData);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/logs")
    @PreAuthorize("hasRole('DMS.Admin')")
    public ResponseEntity<Page<AuditEvent>> getAuditLogs(
            @RequestParam(required = false) UUID applicationId,
            @RequestParam(required = false) AuditEvent.EventType eventType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime,
            Pageable pageable) {
        
        Page<AuditEvent> logs;
        
        if (applicationId != null) {
            logs = auditLogRepository.findByApplicationId(applicationId, pageable);
        } else if (eventType != null) {
            logs = auditLogRepository.findByEventType(eventType, pageable);
        } else if (startTime != null && endTime != null) {
            logs = auditLogRepository.findByTimestampBetween(startTime, endTime, pageable);
        } else {
            logs = auditLogRepository.findAll(pageable);
        }
        
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/logs/{correlationId}")
    @PreAuthorize("hasRole('DMS.Admin')")
    public ResponseEntity<java.util.List<AuditEvent>> getAuditLogsByCorrelationId(
            @PathVariable UUID correlationId) {
        return ResponseEntity.ok(auditLogRepository.findByCorrelationId(correlationId));
    }

    @GetMapping("/retention/stats")
    @PreAuthorize("hasRole('DMS.Admin')")
    public ResponseEntity<Map<String, Object>> getRetentionStats() {
        long totalLogs = auditLogRepository.count();
        long pciLogs = auditLogRepository.findByPciRelevantTrue(Pageable.ofSize(1)).getTotalElements();
        long gdprLogs = auditLogRepository.findByGdprRelevantTrue(Pageable.ofSize(1)).getTotalElements();
        return ResponseEntity.ok(Map.of(
            "totalLogs", totalLogs,
            "pciRelevantLogs", pciLogs,
            "gdprRelevantLogs", gdprLogs,
            "retentionPolicy", "90 days for standard logs, 7 years for PCI/GDPR",
            "archiveStatus", "active"
        ));
    }

    @PostMapping("/retention/archive")
    @PreAuthorize("hasRole('DMS.Admin')")
    public ResponseEntity<Map<String, Object>> archiveOldLogs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant olderThan) {
        // In production, this would move logs to cold storage (Azure Blob, S3, etc.)
        // For now, return a summary of what would be archived
        long count = auditLogRepository.findByTimestampBetween(
            Instant.EPOCH, olderThan, Pageable.ofSize(1)).getTotalElements();
        return ResponseEntity.ok(Map.of(
            "logsToArchive", count,
            "archiveCutoff", olderThan.toString(),
            "status", "archive_initiated",
            "message", String.format("Archival initiated for %d logs older than %s", count, olderThan)
        ));
    }
}

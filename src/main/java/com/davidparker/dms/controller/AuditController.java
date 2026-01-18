package com.davidparker.dms.controller;

import com.davidparker.dms.model.AuditEvent;
import com.davidparker.dms.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    public AuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
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
}

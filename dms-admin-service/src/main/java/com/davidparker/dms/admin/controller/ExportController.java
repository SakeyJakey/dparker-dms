package com.davidparker.dms.admin.controller;

import com.davidparker.dms.admin.service.ExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/export")
@PreAuthorize("hasRole('DMS.Admin')")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/users/csv")
    public ResponseEntity<byte[]> exportUsersCsv() {
        byte[] csv = exportService.exportUsersCsv();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users-export.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv);
    }

    @GetMapping("/audit/csv")
    public ResponseEntity<byte[]> exportAuditCsv(
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        byte[] csv = exportService.exportAuditCsv(eventType, startDate, endDate);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=audit-export.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv);
    }

    @GetMapping("/compliance/report")
    public ResponseEntity<byte[]> exportComplianceReport(
            @RequestParam(defaultValue = "csv") String format) {
        byte[] report = exportService.exportComplianceReport(format);
        String contentType = "pdf".equals(format) ? "application/pdf" : "text/csv";
        String ext = "pdf".equals(format) ? "pdf" : "csv";
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=compliance-report." + ext)
            .contentType(MediaType.parseMediaType(contentType))
            .body(report);
    }
}

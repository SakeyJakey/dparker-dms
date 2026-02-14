package com.davidparker.dms.compliance.controller;

import com.davidparker.dms.compliance.dto.DataExportResponse;
import com.davidparker.dms.compliance.dto.ErasureRequest;
import com.davidparker.dms.compliance.dto.ErasureResponse;
import com.davidparker.dms.compliance.service.GdprComplianceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/compliance")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Compliance", description = "PCI-DSS, GDPR, and ISO 27001 compliance reporting and controls")
public class ComplianceController {

    private final GdprComplianceService gdprComplianceService;

    public ComplianceController(GdprComplianceService gdprComplianceService) {
        this.gdprComplianceService = gdprComplianceService;
    }

    @GetMapping("/pci/report")
    @PreAuthorize("hasRole('DMS.Admin')")
    public ResponseEntity<Map<String, Object>> getPciReport(
            @RequestParam(required = false, defaultValue = "MONTHLY") String period) {
        return ResponseEntity.ok(Map.of(
            "status", "operational",
            "period", period,
            "message", "PCI compliance report endpoint"
        ));
    }

    @GetMapping("/gdpr/data-subject/{id}")
    @PreAuthorize("hasRole('DMS.Admin')")
    public ResponseEntity<DataExportResponse> getDataSubjectData(@PathVariable UUID id) {
        DataExportResponse response = gdprComplianceService.exportDataSubjectData(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/gdpr/data-subject/{id}")
    @PreAuthorize("hasRole('DMS.Admin')")
    public ResponseEntity<ErasureResponse> processErasureRequest(@PathVariable UUID id) {
        ErasureRequest request = new ErasureRequest();
        request.setDataSubjectId(id);
        ErasureResponse response = gdprComplianceService.processErasureRequest(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/iso27001/controls")
    @PreAuthorize("hasRole('DMS.Admin')")
    public ResponseEntity<Map<String, Object>> getIso27001Controls() {
        return ResponseEntity.ok(Map.of(
            "status", "operational",
            "message", "ISO 27001 controls status endpoint"
        ));
    }
}

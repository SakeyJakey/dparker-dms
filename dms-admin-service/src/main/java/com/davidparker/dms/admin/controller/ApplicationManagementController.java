package com.davidparker.dms.admin.controller;

import com.davidparker.dms.admin.dto.ApplicationProvisionRequest;
import com.davidparker.dms.admin.model.RegisteredApplication;
import com.davidparker.dms.admin.service.ApplicationManagementService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/applications")
@PreAuthorize("hasRole('DMS.Admin')")
@Tag(name = "Application Management", description = "Provision and manage registered applications")
public class ApplicationManagementController {

    private final ApplicationManagementService applicationManagementService;

    public ApplicationManagementController(ApplicationManagementService applicationManagementService) {
        this.applicationManagementService = applicationManagementService;
    }

    @GetMapping
    public ResponseEntity<Page<RegisteredApplication>> listApplications(Pageable pageable) {
        return ResponseEntity.ok(applicationManagementService.listApplications(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegisteredApplication> getApplication(@PathVariable UUID id) {
        return ResponseEntity.ok(applicationManagementService.getApplication(id));
    }

    @PostMapping
    public ResponseEntity<RegisteredApplication> provisionApplication(@Valid @RequestBody ApplicationProvisionRequest request) {
        return ResponseEntity.ok(applicationManagementService.provisionApplication(request));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<RegisteredApplication> updateApplicationStatus(
            @PathVariable UUID id,
            @RequestParam RegisteredApplication.ApplicationStatus status) {
        return ResponseEntity.ok(applicationManagementService.updateApplicationStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deprovisionApplication(@PathVariable UUID id) {
        applicationManagementService.deprovisionApplication(id);
        return ResponseEntity.noContent().build();
    }
}

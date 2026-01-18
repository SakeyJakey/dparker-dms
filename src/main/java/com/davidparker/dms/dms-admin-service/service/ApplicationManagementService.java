package com.davidparker.dms.dms_admin_service.service;

import com.davidparker.dms.dms_admin_service.dto.ApplicationProvisionRequest;
import com.davidparker.dms.model.AuditEvent;
import com.davidparker.dms.model.RegisteredApplication;
import com.davidparker.dms.repository.RegisteredApplicationRepository;
import com.davidparker.dms.service.ApplicationProvisioningService;
import com.davidparker.dms.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ApplicationManagementService {

    private final RegisteredApplicationRepository applicationRepository;
    private final ApplicationProvisioningService provisioningService;
    private final AuditService auditService;

    public ApplicationManagementService(
            RegisteredApplicationRepository applicationRepository,
            ApplicationProvisioningService provisioningService,
            AuditService auditService) {
        this.applicationRepository = applicationRepository;
        this.provisioningService = provisioningService;
        this.auditService = auditService;
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    public Page<RegisteredApplication> listApplications(Pageable pageable) {
        return applicationRepository.findAll(pageable);
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    public RegisteredApplication getApplication(UUID id) {
        return applicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Application not found"));
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    @Transactional
    public RegisteredApplication provisionApplication(ApplicationProvisionRequest request) {
        ApplicationProvisioningService.ApplicationProvisionRequest provisionRequest =
            new ApplicationProvisioningService.ApplicationProvisionRequest();
        provisionRequest.setEntraAppId(request.getEntraAppId());
        provisionRequest.setApplicationName(request.getApplicationName());

        RegisteredApplication app = provisioningService.provisionApplication(provisionRequest);

        auditService.logEvent(AuditEvent.builder()
            .eventType(AuditEvent.EventType.APP_PROVISION)
            .eventCategory(AuditEvent.EventCategory.ADMIN_ACTIONS)
            .action("APPLICATION_PROVISIONED")
            .result(AuditEvent.AuditResult.SUCCESS)
            .resourceType("Application")
            .resourceId(app.getId())
            .resourceName(app.getApplicationName())
            .build());

        return app;
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    @Transactional
    public RegisteredApplication updateApplicationStatus(UUID id, RegisteredApplication.ApplicationStatus status) {
        RegisteredApplication app = applicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Application not found"));

        app.setStatus(status);
        app = applicationRepository.save(app);

        auditService.logEvent(AuditEvent.builder()
            .eventType(AuditEvent.EventType.UPDATE)
            .eventCategory(AuditEvent.EventCategory.ADMIN_ACTIONS)
            .action("APPLICATION_STATUS_UPDATED")
            .result(AuditEvent.AuditResult.SUCCESS)
            .resourceType("Application")
            .resourceId(id)
            .resourceName(app.getApplicationName())
            .details(java.util.Map.of("status", status.name()))
            .build());

        return app;
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    @Transactional
    public void deprovisionApplication(UUID id) {
        RegisteredApplication app = applicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Application not found"));

        app.setStatus(RegisteredApplication.ApplicationStatus.INACTIVE);
        applicationRepository.save(app);

        auditService.logEvent(AuditEvent.builder()
            .eventType(AuditEvent.EventType.DELETE)
            .eventCategory(AuditEvent.EventCategory.ADMIN_ACTIONS)
            .action("APPLICATION_DEPROVISIONED")
            .result(AuditEvent.AuditResult.SUCCESS)
            .resourceType("Application")
            .resourceId(id)
            .resourceName(app.getApplicationName())
            .build());
    }
}

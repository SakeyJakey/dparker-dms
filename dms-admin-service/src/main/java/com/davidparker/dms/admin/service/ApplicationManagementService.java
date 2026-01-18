package com.davidparker.dms.admin.service;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.davidparker.dms.admin.dto.ApplicationProvisionRequest;
import com.davidparker.dms.admin.model.RegisteredApplication;
import com.davidparker.dms.admin.repository.RegisteredApplicationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ApplicationManagementService {

    private final RegisteredApplicationRepository applicationRepository;
    private final BlobServiceClient blobServiceClient;
    private final AuditEventClient auditEventClient;

    public ApplicationManagementService(
            RegisteredApplicationRepository applicationRepository,
            @Value("${spring.cloud.azure.storage.blob.account-name:}") String accountName,
            @Value("${spring.cloud.azure.storage.blob.account-key:}") String accountKey,
            AuditEventClient auditEventClient) {
        this.applicationRepository = applicationRepository;
        this.auditEventClient = auditEventClient;
        
        if (accountName != null && !accountName.isEmpty() && accountKey != null && !accountKey.isEmpty()) {
            this.blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(String.format("DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net", accountName, accountKey))
                .buildClient();
        } else {
            this.blobServiceClient = null;
        }
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
        // Check if application already exists
        if (applicationRepository.existsByEntraAppId(request.getEntraAppId())) {
            throw new RuntimeException("Application with Entra App ID already exists");
        }
        if (applicationRepository.existsByApplicationName(request.getApplicationName())) {
            throw new RuntimeException("Application with this name already exists");
        }

        String containerName = "davidparker-lv-bmth-documents";
        String encryptionKeyName = "davidparker-lv-bmth-encryption-key";

        // Create storage container if it doesn't exist
        if (blobServiceClient != null) {
            BlobContainerClient container = blobServiceClient.getBlobContainerClient(containerName);
            if (!container.exists()) {
                container.create();
            }
        }

        RegisteredApplication application = RegisteredApplication.builder()
            .entraAppId(request.getEntraAppId())
            .applicationName(request.getApplicationName())
            .storageContainerName(containerName)
            .encryptionKeyName(encryptionKeyName)
            .status(RegisteredApplication.ApplicationStatus.ACTIVE)
            .build();

        application = applicationRepository.save(application);

        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("eventType", "APP_PROVISION");
        auditEvent.put("eventCategory", "ADMIN_ACTIONS");
        auditEvent.put("action", "APPLICATION_PROVISIONED");
        auditEvent.put("result", "SUCCESS");
        auditEvent.put("resourceType", "Application");
        auditEvent.put("resourceId", application.getId().toString());
        auditEvent.put("resourceName", application.getApplicationName());
        auditEvent.put("timestamp", Instant.now().toString());
        auditEventClient.logEvent(auditEvent);

        return application;
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    @Transactional
    public RegisteredApplication updateApplicationStatus(UUID id, RegisteredApplication.ApplicationStatus status) {
        RegisteredApplication application = applicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Application not found"));

        application.setStatus(status);
        application = applicationRepository.save(application);

        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("eventType", "UPDATE");
        auditEvent.put("eventCategory", "ADMIN_ACTIONS");
        auditEvent.put("action", "APPLICATION_STATUS_UPDATED");
        auditEvent.put("result", "SUCCESS");
        auditEvent.put("resourceType", "Application");
        auditEvent.put("resourceId", id.toString());
        auditEvent.put("details", Map.of("newStatus", status.toString()));
        auditEvent.put("timestamp", Instant.now().toString());
        auditEventClient.logEvent(auditEvent);

        return application;
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    @Transactional
    public void deprovisionApplication(UUID id) {
        RegisteredApplication application = applicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Application not found"));

        applicationRepository.delete(application);

        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("eventType", "DELETE");
        auditEvent.put("eventCategory", "ADMIN_ACTIONS");
        auditEvent.put("action", "APPLICATION_DEPROVISIONED");
        auditEvent.put("result", "SUCCESS");
        auditEvent.put("resourceType", "Application");
        auditEvent.put("resourceId", id.toString());
        auditEvent.put("timestamp", Instant.now().toString());
        auditEventClient.logEvent(auditEvent);
    }
}

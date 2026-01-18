package com.davidparker.dms.admin.service;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.davidparker.dms.admin.dto.ApplicationProvisionRequest;
import com.davidparker.dms.admin.model.RegisteredApplication;
import com.davidparker.dms.admin.repository.RegisteredApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationManagementServiceTest {

    @Mock
    private RegisteredApplicationRepository applicationRepository;

    @Mock
    private BlobServiceClient blobServiceClient;

    @Mock
    private BlobContainerClient blobContainerClient;

    @Mock
    private AuditEventClient auditEventClient;

    @InjectMocks
    private ApplicationManagementService applicationManagementService;

    private RegisteredApplication testApplication;
    private UUID applicationId;

    @BeforeEach
    void setUp() {
        applicationId = UUID.randomUUID();
        ReflectionTestUtils.setField(applicationManagementService, "blobServiceClient", blobServiceClient);

        testApplication = RegisteredApplication.builder()
            .id(applicationId)
            .entraAppId("test-app-id")
            .applicationName("Test Application")
            .storageContainerName("davidparker-lv-bmth-documents")
            .encryptionKeyName("davidparker-lv-bmth-encryption-key")
            .status(RegisteredApplication.ApplicationStatus.ACTIVE)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    }

    @Test
    void testListApplications() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<RegisteredApplication> appPage = new PageImpl<>(List.of(testApplication), pageable, 1);

        when(applicationRepository.findAll(pageable)).thenReturn(appPage);

        Page<RegisteredApplication> result = applicationManagementService.listApplications(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testApplication, result.getContent().get(0));
        verify(applicationRepository).findAll(pageable);
    }

    @Test
    void testGetApplication_Success() {
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));

        RegisteredApplication result = applicationManagementService.getApplication(applicationId);

        assertNotNull(result);
        assertEquals(testApplication.getId(), result.getId());
        verify(applicationRepository).findById(applicationId);
    }

    @Test
    void testGetApplication_NotFound() {
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> applicationManagementService.getApplication(applicationId));
        verify(applicationRepository).findById(applicationId);
    }

    @Test
    void testProvisionApplication() {
        ApplicationProvisionRequest request = new ApplicationProvisionRequest();
        request.setEntraAppId("new-app-id");
        request.setApplicationName("New Application");

        when(applicationRepository.existsByEntraAppId(anyString())).thenReturn(false);
        when(applicationRepository.existsByApplicationName(anyString())).thenReturn(false);
        when(blobServiceClient.getBlobContainerClient(anyString())).thenReturn(blobContainerClient);
        when(blobContainerClient.exists()).thenReturn(false);
        when(applicationRepository.save(any(RegisteredApplication.class))).thenReturn(testApplication);
        doNothing().when(auditEventClient).logEvent(any());

        RegisteredApplication result = applicationManagementService.provisionApplication(request);

        assertNotNull(result);
        verify(applicationRepository).existsByEntraAppId(request.getEntraAppId());
        verify(applicationRepository).existsByApplicationName(request.getApplicationName());
        verify(applicationRepository).save(any(RegisteredApplication.class));
        verify(auditEventClient).logEvent(any());
    }

    @Test
    void testProvisionApplication_DuplicateEntraAppId() {
        ApplicationProvisionRequest request = new ApplicationProvisionRequest();
        request.setEntraAppId("existing-app-id");
        request.setApplicationName("New Application");

        when(applicationRepository.existsByEntraAppId(anyString())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> applicationManagementService.provisionApplication(request));
        verify(applicationRepository).existsByEntraAppId(request.getEntraAppId());
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testUpdateApplicationStatus() {
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));
        when(applicationRepository.save(any(RegisteredApplication.class))).thenReturn(testApplication);
        doNothing().when(auditEventClient).logEvent(any());

        RegisteredApplication result = applicationManagementService.updateApplicationStatus(
            applicationId, RegisteredApplication.ApplicationStatus.SUSPENDED);

        assertNotNull(result);
        assertEquals(RegisteredApplication.ApplicationStatus.SUSPENDED, result.getStatus());
        verify(applicationRepository).findById(applicationId);
        verify(applicationRepository).save(any(RegisteredApplication.class));
        verify(auditEventClient).logEvent(any());
    }

    @Test
    void testDeprovisionApplication() {
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));
        doNothing().when(applicationRepository).delete(testApplication);
        doNothing().when(auditEventClient).logEvent(any());

        applicationManagementService.deprovisionApplication(applicationId);

        verify(applicationRepository).findById(applicationId);
        verify(applicationRepository).delete(testApplication);
        verify(auditEventClient).logEvent(any());
    }
}

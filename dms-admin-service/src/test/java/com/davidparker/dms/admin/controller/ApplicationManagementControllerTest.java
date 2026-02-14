package com.davidparker.dms.admin.controller;

import com.davidparker.dms.admin.dto.ApplicationProvisionRequest;
import com.davidparker.dms.admin.model.RegisteredApplication;
import com.davidparker.dms.admin.service.ApplicationManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ApplicationManagementController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {ApplicationManagementController.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.cloud.azure.keyvault.secret.enabled=false",
    "spring.cloud.azure.keyvault.secret.property-sources[0].enabled=false"
})
class ApplicationManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationManagementService applicationManagementService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UUID appId = UUID.randomUUID();

    private RegisteredApplication createTestApplication() {
        return RegisteredApplication.builder()
            .id(appId)
            .entraAppId("entra-123")
            .applicationName("Test Application")
            .storageContainerName("davidparker-lv-bmth-documents")
            .encryptionKeyName("davidparker-lv-bmth-encryption-key")
            .status(RegisteredApplication.ApplicationStatus.ACTIVE)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    }

    @Test
    void testListApplications() throws Exception {
        RegisteredApplication app = createTestApplication();
        Page<RegisteredApplication> page = new PageImpl<>(List.of(app), PageRequest.of(0, 10), 1);
        when(applicationManagementService.listApplications(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/applications").param("page", "0").param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());

        verify(applicationManagementService).listApplications(any());
    }

    @Test
    void testGetApplication() throws Exception {
        RegisteredApplication app = createTestApplication();
        when(applicationManagementService.getApplication(appId)).thenReturn(app);

        mockMvc.perform(get("/api/v1/admin/applications/{id}", appId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.applicationName").value("Test Application"));

        verify(applicationManagementService).getApplication(appId);
    }

    @Test
    void testProvisionApplication() throws Exception {
        ApplicationProvisionRequest request = new ApplicationProvisionRequest();
        request.setEntraAppId("entra-456");
        request.setApplicationName("New Application");

        RegisteredApplication created = createTestApplication();
        when(applicationManagementService.provisionApplication(any(ApplicationProvisionRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/v1/admin/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        verify(applicationManagementService).provisionApplication(any(ApplicationProvisionRequest.class));
    }

    @Test
    void testUpdateApplicationStatus() throws Exception {
        RegisteredApplication app = createTestApplication();
        when(applicationManagementService.updateApplicationStatus(appId, RegisteredApplication.ApplicationStatus.INACTIVE)).thenReturn(app);

        mockMvc.perform(put("/api/v1/admin/applications/{id}/status", appId)
                .param("status", "INACTIVE"))
            .andExpect(status().isOk());

        verify(applicationManagementService).updateApplicationStatus(appId, RegisteredApplication.ApplicationStatus.INACTIVE);
    }

    @Test
    void testDeprovisionApplication() throws Exception {
        doNothing().when(applicationManagementService).deprovisionApplication(appId);

        mockMvc.perform(delete("/api/v1/admin/applications/{id}", appId))
            .andExpect(status().isNoContent());

        verify(applicationManagementService).deprovisionApplication(appId);
    }
}

package com.davidparker.dms.admin.controller;

import com.davidparker.dms.admin.dto.ApplicationProvisionRequest;
import com.davidparker.dms.admin.model.RegisteredApplication;
import com.davidparker.dms.admin.service.ApplicationManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApplicationManagementController.class)
class ApplicationManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationManagementService applicationManagementService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID applicationId = UUID.randomUUID();

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testListApplications() throws Exception {
        RegisteredApplication app = RegisteredApplication.builder()
            .id(applicationId)
            .applicationName("Test App")
            .entraAppId("entra-123")
            .status(RegisteredApplication.ApplicationStatus.ACTIVE)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        Page<RegisteredApplication> appPage = new PageImpl<>(List.of(app), PageRequest.of(0, 10), 1);

        when(applicationManagementService.listApplications(any())).thenReturn(appPage);

        mockMvc.perform(get("/api/v1/admin/applications")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].name").value("Test App"));

        verify(applicationManagementService).listApplications(any());
    }

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testGetApplication() throws Exception {
        RegisteredApplication app = RegisteredApplication.builder()
            .id(applicationId)
            .applicationName("Test App")
            .entraAppId("entra-123")
            .status(RegisteredApplication.ApplicationStatus.ACTIVE)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        when(applicationManagementService.getApplication(applicationId)).thenReturn(app);

        mockMvc.perform(get("/api/v1/admin/applications/{id}", applicationId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(applicationId.toString()))
            .andExpect(jsonPath("$.applicationName").value("Test App"));

        verify(applicationManagementService).getApplication(applicationId);
    }

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testProvisionApplication() throws Exception {
        ApplicationProvisionRequest request = new ApplicationProvisionRequest();
        request.setApplicationName("New App");
        request.setEntraAppId("entra-456");

        RegisteredApplication app = RegisteredApplication.builder()
            .id(applicationId)
            .applicationName("New App")
            .entraAppId("entra-456")
            .status(RegisteredApplication.ApplicationStatus.ACTIVE)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        when(applicationManagementService.provisionApplication(any(ApplicationProvisionRequest.class)))
            .thenReturn(app);

        mockMvc.perform(post("/api/v1/admin/applications")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.applicationName").value("New App"));

        verify(applicationManagementService).provisionApplication(any(ApplicationProvisionRequest.class));
    }

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testUpdateApplicationStatus() throws Exception {
        RegisteredApplication app = RegisteredApplication.builder()
            .id(applicationId)
            .applicationName("Test App")
            .status(RegisteredApplication.ApplicationStatus.SUSPENDED)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        when(applicationManagementService.updateApplicationStatus(
            eq(applicationId), eq(RegisteredApplication.ApplicationStatus.SUSPENDED))).thenReturn(app);

        mockMvc.perform(put("/api/v1/admin/applications/{id}/status", applicationId)
                .with(csrf())
                .param("status", "SUSPENDED"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUSPENDED"));

        verify(applicationManagementService).updateApplicationStatus(
            eq(applicationId), eq(RegisteredApplication.ApplicationStatus.SUSPENDED));
    }

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testDeprovisionApplication() throws Exception {
        doNothing().when(applicationManagementService).deprovisionApplication(applicationId);

        mockMvc.perform(delete("/api/v1/admin/applications/{id}", applicationId)
                .with(csrf()))
            .andExpect(status().isNoContent());

        verify(applicationManagementService).deprovisionApplication(applicationId);
    }
}

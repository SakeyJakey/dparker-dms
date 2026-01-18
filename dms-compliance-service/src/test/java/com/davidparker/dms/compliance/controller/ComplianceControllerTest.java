package com.davidparker.dms.compliance.controller;

import com.davidparker.dms.compliance.dto.DataExportResponse;
import com.davidparker.dms.compliance.dto.ErasureResponse;
import com.davidparker.dms.compliance.service.GdprComplianceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ComplianceController.class)
class ComplianceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GdprComplianceService gdprComplianceService;

    private UUID dataSubjectId = UUID.randomUUID();

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testGetPciReport() throws Exception {
        mockMvc.perform(get("/api/v1/compliance/pci/report"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("operational"))
            .andExpect(jsonPath("$.period").value("MONTHLY"));
    }

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testGetPciReportWithPeriod() throws Exception {
        mockMvc.perform(get("/api/v1/compliance/pci/report")
                .param("period", "WEEKLY"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("operational"))
            .andExpect(jsonPath("$.period").value("WEEKLY"));
    }

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testGetDataSubjectData() throws Exception {
        DataExportResponse response = new DataExportResponse("exports/path", Duration.ofHours(24));
        when(gdprComplianceService.exportDataSubjectData(dataSubjectId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/compliance/gdpr/data-subject/{id}", dataSubjectId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exportPath").value("exports/path"));
    }

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testProcessErasureRequest() throws Exception {
        ErasureResponse response = new ErasureResponse(5, 2);
        when(gdprComplianceService.processErasureRequest(any())).thenReturn(response);

        mockMvc.perform(delete("/api/v1/compliance/gdpr/data-subject/{id}", dataSubjectId)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.documentsDeleted").value(5))
            .andExpect(jsonPath("$.documentsRetained").value(2));
    }

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testGetIso27001Controls() throws Exception {
        mockMvc.perform(get("/api/v1/compliance/iso27001/controls"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("operational"));
    }
}

package com.davidparker.dms.compliance.controller;

import com.davidparker.dms.compliance.dto.DataExportResponse;
import com.davidparker.dms.compliance.dto.ErasureResponse;
import com.davidparker.dms.compliance.service.GdprComplianceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ComplianceController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {ComplianceController.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.cloud.azure.keyvault.secret.enabled=false",
    "spring.cloud.azure.keyvault.secret.property-sources[0].enabled=false"
})
class ComplianceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GdprComplianceService gdprComplianceService;

    private final UUID dataSubjectId = UUID.randomUUID();

    @Test
    void testGetPciReport() throws Exception {
        mockMvc.perform(get("/api/v1/compliance/pci/report"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("operational"));
    }

    @Test
    void testGetPciReportWithPeriod() throws Exception {
        mockMvc.perform(get("/api/v1/compliance/pci/report").param("period", "QUARTERLY"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.period").value("QUARTERLY"));
    }

    @Test
    void testGetDataSubjectData() throws Exception {
        DataExportResponse response = new DataExportResponse("exports/path", Duration.ofHours(24));
        when(gdprComplianceService.exportDataSubjectData(dataSubjectId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/compliance/gdpr/data-subject/{id}", dataSubjectId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exportPath").value("exports/path"));

        verify(gdprComplianceService).exportDataSubjectData(dataSubjectId);
    }

    @Test
    void testProcessErasureRequest() throws Exception {
        ErasureResponse response = new ErasureResponse(5, 2);
        when(gdprComplianceService.processErasureRequest(any())).thenReturn(response);

        mockMvc.perform(delete("/api/v1/compliance/gdpr/data-subject/{id}", dataSubjectId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.deletedCount").value(5))
            .andExpect(jsonPath("$.retainedCount").value(2));

        verify(gdprComplianceService).processErasureRequest(any());
    }

    @Test
    void testGetIso27001Controls() throws Exception {
        mockMvc.perform(get("/api/v1/compliance/iso27001/controls"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("operational"));
    }
}

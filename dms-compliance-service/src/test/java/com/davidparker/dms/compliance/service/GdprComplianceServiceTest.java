package com.davidparker.dms.compliance.service;

import com.davidparker.dms.compliance.dto.DataExportResponse;
import com.davidparker.dms.compliance.dto.ErasureRequest;
import com.davidparker.dms.compliance.dto.ErasureResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GdprComplianceServiceTest {

    @Mock
    private AuditEventClient auditEventClient;

    @Mock
    private WebClient documentServiceClient;

    @InjectMocks
    private GdprComplianceService gdprComplianceService;

    private UUID dataSubjectId;

    @BeforeEach
    void setUp() {
        dataSubjectId = UUID.randomUUID();
        gdprComplianceService = new GdprComplianceService(auditEventClient, "http://localhost:8083");
    }

    @Test
    void testProcessErasureRequest() {
        ErasureRequest request = new ErasureRequest();
        request.setDataSubjectId(dataSubjectId);

        ErasureResponse response = gdprComplianceService.processErasureRequest(request);

        assertNotNull(response);
        assertTrue(response.getDeletedCount() >= 0);
        assertTrue(response.getRetainedCount() >= 0);
        verify(auditEventClient).logEvent(any());
    }

    @Test
    void testExportDataSubjectData() {
        DataExportResponse response = gdprComplianceService.exportDataSubjectData(dataSubjectId);

        assertNotNull(response);
        assertNotNull(response.getExportPath());
        assertEquals(Duration.ofHours(24), response.getExpiration());
        verify(auditEventClient).logEvent(any());
    }
}

package com.davidparker.dms.audit.controller;

import com.davidparker.dms.audit.model.AuditEvent;
import com.davidparker.dms.audit.repository.AuditLogRepository;
import com.davidparker.dms.audit.service.AuditService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuditController.class)
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditLogRepository auditLogRepository;

    @MockBean
    private AuditService auditService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID auditEventId = UUID.randomUUID();
    private UUID applicationId = UUID.randomUUID();

    @Test
    void testLogEvent() throws Exception {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventType", "CREATE");
        eventData.put("eventCategory", "DOCUMENT_LIFECYCLE");
        eventData.put("action", "DOCUMENT_UPLOADED");
        eventData.put("result", "SUCCESS");

        doNothing().when(auditService).logEvent(any());

        mockMvc.perform(post("/api/v1/audit/events")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventData)))
            .andExpect(status().isAccepted());

        verify(auditService).logEvent(any());
    }

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testGetAuditLogs() throws Exception {
        AuditEvent event = AuditEvent.builder()
            .id(auditEventId)
            .eventId("event-123")
            .eventType(AuditEvent.EventType.CREATE)
            .eventCategory(AuditEvent.EventCategory.DOCUMENT_LIFECYCLE)
            .action("DOCUMENT_UPLOADED")
            .result(AuditEvent.AuditResult.SUCCESS)
            .timestamp(Instant.now())
            .applicationId(applicationId)
            .checksum("abc123")
            .build();

        Page<AuditEvent> eventPage = new PageImpl<>(List.of(event), PageRequest.of(0, 10), 1);

        when(auditLogRepository.findAll(any(PageRequest.class))).thenReturn(eventPage);

        mockMvc.perform(get("/api/v1/audit/logs")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].eventType").value("CREATE"));

        verify(auditLogRepository).findAll(any(PageRequest.class));
    }

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testGetAuditLogsByApplicationId() throws Exception {
        AuditEvent event = AuditEvent.builder()
            .id(auditEventId)
            .eventId("event-123")
            .eventType(AuditEvent.EventType.CREATE)
            .eventCategory(AuditEvent.EventCategory.DOCUMENT_LIFECYCLE)
            .action("DOCUMENT_UPLOADED")
            .result(AuditEvent.AuditResult.SUCCESS)
            .timestamp(Instant.now())
            .applicationId(applicationId)
            .checksum("abc123")
            .build();

        Page<AuditEvent> eventPage = new PageImpl<>(List.of(event), PageRequest.of(0, 10), 1);

        when(auditLogRepository.findByApplicationId(applicationId, PageRequest.of(0, 10)))
            .thenReturn(eventPage);

        mockMvc.perform(get("/api/v1/audit/logs")
                .param("applicationId", applicationId.toString())
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].applicationId").value(applicationId.toString()));

        verify(auditLogRepository).findByApplicationId(applicationId, PageRequest.of(0, 10));
    }

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testGetAuditLogsByEventType() throws Exception {
        AuditEvent event = AuditEvent.builder()
            .id(auditEventId)
            .eventId("event-123")
            .eventType(AuditEvent.EventType.CREATE)
            .eventCategory(AuditEvent.EventCategory.DOCUMENT_LIFECYCLE)
            .action("DOCUMENT_UPLOADED")
            .result(AuditEvent.AuditResult.SUCCESS)
            .timestamp(Instant.now())
            .checksum("abc123")
            .build();

        Page<AuditEvent> eventPage = new PageImpl<>(List.of(event), PageRequest.of(0, 10), 1);

        when(auditLogRepository.findByEventType(AuditEvent.EventType.CREATE, PageRequest.of(0, 10)))
            .thenReturn(eventPage);

        mockMvc.perform(get("/api/v1/audit/logs")
                .param("eventType", "CREATE")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].eventType").value("CREATE"));

        verify(auditLogRepository).findByEventType(AuditEvent.EventType.CREATE, PageRequest.of(0, 10));
    }

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testGetAuditLogsByTimeRange() throws Exception {
        AuditEvent event = AuditEvent.builder()
            .id(auditEventId)
            .eventId("event-123")
            .eventType(AuditEvent.EventType.CREATE)
            .eventCategory(AuditEvent.EventCategory.DOCUMENT_LIFECYCLE)
            .action("DOCUMENT_UPLOADED")
            .result(AuditEvent.AuditResult.SUCCESS)
            .timestamp(Instant.now())
            .checksum("abc123")
            .build();

        Page<AuditEvent> eventPage = new PageImpl<>(List.of(event), PageRequest.of(0, 10), 1);

        Instant startTime = Instant.now().minusSeconds(3600);
        Instant endTime = Instant.now();

        when(auditLogRepository.findByTimestampBetween(startTime, endTime, PageRequest.of(0, 10)))
            .thenReturn(eventPage);

        mockMvc.perform(get("/api/v1/audit/logs")
                .param("startTime", startTime.toString())
                .param("endTime", endTime.toString())
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk());

        verify(auditLogRepository).findByTimestampBetween(startTime, endTime, PageRequest.of(0, 10));
    }

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testGetAuditLogsByCorrelationId() throws Exception {
        UUID correlationId = UUID.randomUUID();
        AuditEvent event = AuditEvent.builder()
            .id(auditEventId)
            .eventId("event-123")
            .eventType(AuditEvent.EventType.CREATE)
            .eventCategory(AuditEvent.EventCategory.DOCUMENT_LIFECYCLE)
            .action("DOCUMENT_UPLOADED")
            .result(AuditEvent.AuditResult.SUCCESS)
            .timestamp(Instant.now())
            .correlationId(correlationId)
            .checksum("abc123")
            .build();

        when(auditLogRepository.findByCorrelationId(correlationId)).thenReturn(List.of(event));

        mockMvc.perform(get("/api/v1/audit/logs/{correlationId}", correlationId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].correlationId").value(correlationId.toString()));

        verify(auditLogRepository).findByCorrelationId(correlationId);
    }
}

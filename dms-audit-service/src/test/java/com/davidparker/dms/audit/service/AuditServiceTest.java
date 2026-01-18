package com.davidparker.dms.audit.service;

import com.davidparker.dms.audit.model.AuditEvent;
import com.davidparker.dms.audit.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private ObjectMapper objectMapper;

    private AuditService auditService;

    private Map<String, Object> eventData;

    @BeforeEach
    void setUp() throws Exception {
        eventData = new HashMap<>();
        eventData.put("eventType", "CREATE");
        eventData.put("eventCategory", "DOCUMENT_LIFECYCLE");
        eventData.put("action", "DOCUMENT_UPLOADED");
        eventData.put("result", "SUCCESS");
        eventData.put("resourceId", UUID.randomUUID().toString());
        eventData.put("applicationId", UUID.randomUUID().toString());
        eventData.put("timestamp", Instant.now().toString());

        // Create service with empty connection string (no EventHub)
        auditService = new AuditService(auditLogRepository, objectMapper, "", "audit-logs");
        
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
    }

    @Test
    void testLogEvent() {
        when(auditLogRepository.save(any(AuditEvent.class))).thenAnswer(invocation -> {
            AuditEvent event = invocation.getArgument(0);
            event.setId(UUID.randomUUID());
            return event;
        });

        auditService.logEvent(eventData);

        verify(auditLogRepository).save(any(AuditEvent.class));
    }

    @Test
    void testLogEventWithoutTimestamp() {
        eventData.remove("timestamp");
        when(auditLogRepository.save(any(AuditEvent.class))).thenAnswer(invocation -> {
            AuditEvent event = invocation.getArgument(0);
            event.setId(UUID.randomUUID());
            return event;
        });

        auditService.logEvent(eventData);

        verify(auditLogRepository).save(any(AuditEvent.class));
    }

    @Test
    void testLogEventWithoutEventId() {
        when(auditLogRepository.save(any(AuditEvent.class))).thenAnswer(invocation -> {
            AuditEvent event = invocation.getArgument(0);
            event.setId(UUID.randomUUID());
            return event;
        });

        auditService.logEvent(eventData);

        verify(auditLogRepository).save(any(AuditEvent.class));
    }
}

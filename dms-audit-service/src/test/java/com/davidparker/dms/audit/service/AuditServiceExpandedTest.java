package com.davidparker.dms.audit.service;

import com.davidparker.dms.audit.model.AuditEvent;
import com.davidparker.dms.audit.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceExpandedTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    private AuditService auditService;
    @BeforeEach
    void setUp() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        auditService = new AuditService(auditLogRepository, objectMapper, "", "audit-logs");
    }

    @Test
    void testLogEventSetsTimestampIfMissing() {
        Map<String, Object> eventData = createBaseEventData();
        eventData.remove("timestamp");

        when(auditLogRepository.save(any(AuditEvent.class))).thenAnswer(inv -> {
            AuditEvent e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        auditService.logEvent(eventData);

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditLogRepository).save(captor.capture());
        assertNotNull(captor.getValue().getTimestamp());
    }

    @Test
    void testLogEventGeneratesEventIdIfMissing() {
        Map<String, Object> eventData = createBaseEventData();

        when(auditLogRepository.save(any(AuditEvent.class))).thenAnswer(inv -> {
            AuditEvent e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        auditService.logEvent(eventData);

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditLogRepository).save(captor.capture());
        assertNotNull(captor.getValue().getEventId());
    }

    @Test
    void testLogEventCalculatesChecksum() {
        Map<String, Object> eventData = createBaseEventData();

        when(auditLogRepository.save(any(AuditEvent.class))).thenAnswer(inv -> {
            AuditEvent e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        auditService.logEvent(eventData);

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditLogRepository).save(captor.capture());
        assertNotNull(captor.getValue().getChecksum());
        assertEquals(64, captor.getValue().getChecksum().length()); // SHA-256 hex = 64 chars
    }

    @Test
    void testLogEventPreservesEventType() {
        Map<String, Object> eventData = createBaseEventData();

        when(auditLogRepository.save(any(AuditEvent.class))).thenAnswer(inv -> {
            AuditEvent e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        auditService.logEvent(eventData);

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditLogRepository).save(captor.capture());
        assertEquals(AuditEvent.EventType.CREATE, captor.getValue().getEventType());
    }

    @Test
    void testLogEventPreservesApplicationId() {
        UUID appId = UUID.randomUUID();
        Map<String, Object> eventData = createBaseEventData();
        eventData.put("applicationId", appId.toString());

        when(auditLogRepository.save(any(AuditEvent.class))).thenAnswer(inv -> {
            AuditEvent e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        auditService.logEvent(eventData);

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditLogRepository).save(captor.capture());
        assertEquals(appId, captor.getValue().getApplicationId());
    }

    @Test
    void testLogEventWithResourceId() {
        UUID resourceId = UUID.randomUUID();
        Map<String, Object> eventData = createBaseEventData();
        eventData.put("resourceId", resourceId.toString());

        when(auditLogRepository.save(any(AuditEvent.class))).thenAnswer(inv -> {
            AuditEvent e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        auditService.logEvent(eventData);

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditLogRepository).save(captor.capture());
        assertEquals(resourceId, captor.getValue().getResourceId());
    }

    @Test
    void testLogEventNoEventHubWhenNotConfigured() {
        Map<String, Object> eventData = createBaseEventData();

        when(auditLogRepository.save(any(AuditEvent.class))).thenAnswer(inv -> {
            AuditEvent e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        // Should not throw - EventHub is null
        auditService.logEvent(eventData);

        verify(auditLogRepository).save(any(AuditEvent.class));
    }

    @Test
    void testLogEventMapsAllEventCategories() {
        for (AuditEvent.EventCategory category : AuditEvent.EventCategory.values()) {
            Map<String, Object> eventData = createBaseEventData();
            eventData.put("eventCategory", category.name());

            when(auditLogRepository.save(any(AuditEvent.class))).thenAnswer(inv -> {
                AuditEvent e = inv.getArgument(0);
                e.setId(UUID.randomUUID());
                return e;
            });

            auditService.logEvent(eventData);
        }

        verify(auditLogRepository, times(AuditEvent.EventCategory.values().length)).save(any());
    }

    private Map<String, Object> createBaseEventData() {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventType", "CREATE");
        eventData.put("eventCategory", "DOCUMENT_LIFECYCLE");
        eventData.put("action", "DOCUMENT_UPLOADED");
        eventData.put("result", "SUCCESS");
        eventData.put("timestamp", Instant.now().toString());
        return eventData;
    }
}

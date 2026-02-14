package com.davidparker.dms.core.dto;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuditEventDtoTest {

    @Test
    void testBuilder() {
        UUID userId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        Instant now = Instant.now();

        AuditEventDto dto = AuditEventDto.builder()
            .eventType("CREATE")
            .eventCategory("DOCUMENT_LIFECYCLE")
            .action("DOCUMENT_UPLOADED")
            .result("SUCCESS")
            .timestamp(now)
            .userId(userId)
            .username("testuser")
            .resourceType("Document")
            .resourceId(resourceId)
            .resourceName("test.pdf")
            .pciRelevant(false)
            .gdprRelevant(true)
            .details(Map.of("size", 1024))
            .build();

        assertEquals("CREATE", dto.getEventType());
        assertEquals("DOCUMENT_LIFECYCLE", dto.getEventCategory());
        assertEquals("DOCUMENT_UPLOADED", dto.getAction());
        assertEquals("SUCCESS", dto.getResult());
        assertEquals(now, dto.getTimestamp());
        assertEquals(userId, dto.getUserId());
        assertEquals("testuser", dto.getUsername());
        assertEquals("Document", dto.getResourceType());
        assertEquals(resourceId, dto.getResourceId());
        assertEquals("test.pdf", dto.getResourceName());
        assertFalse(dto.getPciRelevant());
        assertTrue(dto.getGdprRelevant());
        assertNotNull(dto.getDetails());
    }

    @Test
    void testNoArgsConstructor() {
        AuditEventDto dto = new AuditEventDto();
        assertNull(dto.getEventType());
        assertNull(dto.getUserId());
    }

    @Test
    void testSetters() {
        AuditEventDto dto = new AuditEventDto();
        dto.setEventType("UPDATE");
        dto.setAction("DOCUMENT_UPDATED");
        dto.setResult("SUCCESS");

        assertEquals("UPDATE", dto.getEventType());
        assertEquals("DOCUMENT_UPDATED", dto.getAction());
        assertEquals("SUCCESS", dto.getResult());
    }
}

package com.davidparker.dms.llm.service;

import com.davidparker.dms.llm.dto.QueryHistoryEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class QueryHistoryServiceTest {

    private QueryHistoryService service;

    @BeforeEach
    void setUp() {
        service = new QueryHistoryService();
    }

    @Test
    void testRecordAndRetrieveQuery() {
        UUID correlationId = UUID.randomUUID();
        service.recordQuery(correlationId, "test query", 5, "SUCCESS");

        List<QueryHistoryEntry> history = service.getRecentQueries(10);
        assertEquals(1, history.size());
        assertEquals("test query", history.get(0).getQuery());
        assertEquals(5, history.get(0).getResultCount());
        assertEquals("SUCCESS", history.get(0).getStatus());
        assertEquals(correlationId, history.get(0).getCorrelationId());
        assertNotNull(history.get(0).getTimestamp());
    }

    @Test
    void testRecentQueriesOrderedMostRecentFirst() {
        service.recordQuery(UUID.randomUUID(), "first", 1, "SUCCESS");
        service.recordQuery(UUID.randomUUID(), "second", 2, "SUCCESS");
        service.recordQuery(UUID.randomUUID(), "third", 3, "SUCCESS");

        List<QueryHistoryEntry> history = service.getRecentQueries(10);
        assertEquals(3, history.size());
        assertEquals("third", history.get(0).getQuery());
        assertEquals("second", history.get(1).getQuery());
        assertEquals("first", history.get(2).getQuery());
    }

    @Test
    void testLimitReturnsOnlyRequestedCount() {
        for (int i = 0; i < 10; i++) {
            service.recordQuery(UUID.randomUUID(), "query " + i, i, "SUCCESS");
        }

        List<QueryHistoryEntry> history = service.getRecentQueries(3);
        assertEquals(3, history.size());
    }

    @Test
    void testGetTotalQueryCount() {
        assertEquals(0, service.getTotalQueryCount());

        service.recordQuery(UUID.randomUUID(), "q1", 1, "SUCCESS");
        service.recordQuery(UUID.randomUUID(), "q2", 2, "SUCCESS");

        assertEquals(2, service.getTotalQueryCount());
    }

    @Test
    void testEmptyHistoryReturnsEmptyList() {
        List<QueryHistoryEntry> history = service.getRecentQueries(10);
        assertTrue(history.isEmpty());
    }

    @Test
    void testHistoryIsImmutable() {
        service.recordQuery(UUID.randomUUID(), "test", 1, "SUCCESS");
        List<QueryHistoryEntry> history = service.getRecentQueries(10);
        assertThrows(UnsupportedOperationException.class, () -> history.add(null));
    }
}

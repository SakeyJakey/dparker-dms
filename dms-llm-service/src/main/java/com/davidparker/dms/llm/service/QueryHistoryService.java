package com.davidparker.dms.llm.service;

import com.davidparker.dms.llm.dto.QueryHistoryEntry;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Tracks LLM query history for audit and replay purposes.
 * In production, this would be persisted to a database.
 */
@Service
public class QueryHistoryService {

    private static final int MAX_HISTORY_SIZE = 1000;
    private final ConcurrentLinkedDeque<QueryHistoryEntry> history = new ConcurrentLinkedDeque<>();

    public void recordQuery(UUID correlationId, String query, int resultCount, String status) {
        QueryHistoryEntry entry = QueryHistoryEntry.builder()
            .correlationId(correlationId)
            .query(query)
            .resultCount(resultCount)
            .status(status)
            .timestamp(Instant.now())
            .build();
        history.addFirst(entry);
        while (history.size() > MAX_HISTORY_SIZE) {
            history.removeLast();
        }
    }

    public List<QueryHistoryEntry> getRecentQueries(int limit) {
        List<QueryHistoryEntry> result = new ArrayList<>();
        int count = 0;
        for (QueryHistoryEntry entry : history) {
            if (count >= limit) break;
            result.add(entry);
            count++;
        }
        return Collections.unmodifiableList(result);
    }

    public long getTotalQueryCount() {
        return history.size();
    }
}

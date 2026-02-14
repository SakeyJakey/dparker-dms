package com.davidparker.dms.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class QueryHistoryEntry {
    private UUID correlationId;
    private String query;
    private int resultCount;
    private String status;
    private Instant timestamp;
}

package com.davidparker.dms.llm.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class LlmQueryResponse {
    private UUID correlationId;
    private String summary;
    private List<Map<String, Object>> results;
    private Integer totalCount;
}

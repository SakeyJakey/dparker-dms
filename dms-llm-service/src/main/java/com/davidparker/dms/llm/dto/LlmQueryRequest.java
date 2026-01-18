package com.davidparker.dms.llm.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class LlmQueryRequest {
    private String query;
    private Filters filters;
    private Integer maxResults;
    private Boolean includeSummary;
    private UUID applicationId;

    @Data
    public static class Filters {
        private List<String> classifications;
        private DateRange dateRange;
    }

    @Data
    public static class DateRange {
        private String start;
        private String end;
    }
}

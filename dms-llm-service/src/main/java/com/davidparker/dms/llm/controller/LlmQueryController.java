package com.davidparker.dms.llm.controller;

import com.davidparker.dms.llm.dto.LlmQueryRequest;
import com.davidparker.dms.llm.dto.LlmQueryResponse;
import com.davidparker.dms.llm.dto.QueryHistoryEntry;
import com.davidparker.dms.llm.service.QueryHistoryService;
import com.davidparker.dms.llm.service.SecureLlmQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/llm")
@io.swagger.v3.oas.annotations.tags.Tag(name = "LLM Query", description = "AI-powered natural language document queries")
public class LlmQueryController {

    private final SecureLlmQueryService llmQueryService;
    private final QueryHistoryService queryHistoryService;

    public LlmQueryController(SecureLlmQueryService llmQueryService,
                               QueryHistoryService queryHistoryService) {
        this.llmQueryService = llmQueryService;
        this.queryHistoryService = queryHistoryService;
    }

    @PostMapping("/query")
    @PreAuthorize("hasRole('DMS.LLM.Service')")
    public ResponseEntity<LlmQueryResponse> executeQuery(@RequestBody LlmQueryRequest request) {
        LlmQueryResponse response = llmQueryService.executeQuery(request);
        queryHistoryService.recordQuery(
            response.getCorrelationId(),
            request.getQuery(),
            response.getResults() != null ? response.getResults().size() : 0,
            "SUCCESS"
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/compliance-check")
    @PreAuthorize("hasRole('DMS.LLM.Service')")
    public ResponseEntity<LlmQueryResponse> complianceCheck(@RequestBody LlmQueryRequest request) {
        LlmQueryResponse response = llmQueryService.executeQuery(request);
        queryHistoryService.recordQuery(
            response.getCorrelationId(),
            request.getQuery(),
            response.getResults() != null ? response.getResults().size() : 0,
            "COMPLIANCE_CHECK"
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<QueryHistoryEntry>> getQueryHistory(
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(queryHistoryService.getRecentQueries(limit));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getQueryStats() {
        return ResponseEntity.ok(Map.of(
            "totalQueries", queryHistoryService.getTotalQueryCount(),
            "service", "dms-llm-service",
            "status", "operational"
        ));
    }
}

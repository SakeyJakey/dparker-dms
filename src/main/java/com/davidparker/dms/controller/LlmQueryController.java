package com.davidparker.dms.controller;

import com.davidparker.dms.service.ai.SecureLlmQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/llm")
public class LlmQueryController {

    private final SecureLlmQueryService llmQueryService;

    public LlmQueryController(SecureLlmQueryService llmQueryService) {
        this.llmQueryService = llmQueryService;
    }

    @PostMapping("/query")
    @PreAuthorize("hasRole('DMS.LLM.Service')")
    public ResponseEntity<SecureLlmQueryService.LlmQueryResponse> executeQuery(
            @RequestBody SecureLlmQueryService.LlmQueryRequest request) {
        SecureLlmQueryService.LlmQueryResponse response = llmQueryService.executeQuery(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/compliance-check")
    @PreAuthorize("hasRole('DMS.LLM.Service')")
    public ResponseEntity<SecureLlmQueryService.LlmQueryResponse> complianceCheck(
            @RequestBody SecureLlmQueryService.LlmQueryRequest request) {
        // Compliance-focused query
        SecureLlmQueryService.LlmQueryResponse response = llmQueryService.executeQuery(request);
        return ResponseEntity.ok(response);
    }
}

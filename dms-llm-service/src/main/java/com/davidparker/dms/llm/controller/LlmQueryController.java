package com.davidparker.dms.llm.controller;

import com.davidparker.dms.llm.dto.LlmQueryRequest;
import com.davidparker.dms.llm.dto.LlmQueryResponse;
import com.davidparker.dms.llm.service.SecureLlmQueryService;
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
    public ResponseEntity<LlmQueryResponse> executeQuery(@RequestBody LlmQueryRequest request) {
        LlmQueryResponse response = llmQueryService.executeQuery(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/compliance-check")
    @PreAuthorize("hasRole('DMS.LLM.Service')")
    public ResponseEntity<LlmQueryResponse> complianceCheck(@RequestBody LlmQueryRequest request) {
        LlmQueryResponse response = llmQueryService.executeQuery(request);
        return ResponseEntity.ok(response);
    }
}

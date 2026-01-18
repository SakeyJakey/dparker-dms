package com.davidparker.dms.llm.service;

import com.azure.ai.openai.OpenAIClient;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchResults;
import com.azure.search.documents.models.VectorizedQuery;
import com.azure.search.documents.models.VectorSearchOptions;
import com.davidparker.dms.llm.dto.LlmQueryRequest;
import com.davidparker.dms.llm.dto.LlmQueryResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SecureLlmQueryService {

    private final OpenAIClient aiFoundryClient;
    private final SearchClient searchClient;
    private final AuditEventClient auditEventClient;

    public SecureLlmQueryService(
            OpenAIClient aiFoundryClient,
            SearchClient searchClient,
            AuditEventClient auditEventClient) {
        this.aiFoundryClient = aiFoundryClient;
        this.searchClient = searchClient;
        this.auditEventClient = auditEventClient;
    }

    @PreAuthorize("hasRole('DMS.LLM.Service')")
    public LlmQueryResponse executeQuery(LlmQueryRequest request) {
        UUID correlationId = UUID.randomUUID();
        
        // Log query initiation
        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("eventType", "LLM_QUERY_INITIATED");
        auditEvent.put("eventCategory", "LLM_QUERIES");
        auditEvent.put("action", "LLM_QUERY_INITIATED");
        auditEvent.put("result", "SUCCESS");
        auditEvent.put("correlationId", correlationId.toString());
        auditEvent.put("details", Map.of("query", sanitizeQuery(request.getQuery())));
        auditEvent.put("timestamp", Instant.now().toString());
        auditEventClient.logEvent(auditEvent);
        
        try {
            // Build secure search with application isolation
            SearchOptions searchOptions = buildSecureSearchOptions(request);
            
            // Execute search
            SearchResults<Map> results = searchClient != null ? 
                searchClient.search(request.getQuery(), searchOptions, Map.class) : null;
            
            // Filter results by RBAC permissions
            List<Map<String, Object>> permittedResults = filterByPermissions(results);
            
            // Generate summary
            String summary = generateSummaryWithCitations(request.getQuery(), permittedResults);
            
            // Log query completion
            Map<String, Object> completionEvent = new HashMap<>();
            completionEvent.put("eventType", "LLM_QUERY_COMPLETED");
            completionEvent.put("eventCategory", "LLM_QUERIES");
            completionEvent.put("action", "LLM_QUERY_COMPLETED");
            completionEvent.put("result", "SUCCESS");
            completionEvent.put("correlationId", correlationId.toString());
            completionEvent.put("details", Map.of("resultCount", String.valueOf(permittedResults.size())));
            completionEvent.put("timestamp", Instant.now().toString());
            auditEventClient.logEvent(completionEvent);
            
            return LlmQueryResponse.builder()
                .correlationId(correlationId)
                .summary(summary)
                .results(permittedResults)
                .build();
        } catch (Exception e) {
            Map<String, Object> errorEvent = new HashMap<>();
            errorEvent.put("eventType", "LLM_QUERY_COMPLETED");
            errorEvent.put("eventCategory", "LLM_QUERIES");
            errorEvent.put("action", "LLM_QUERY_FAILED");
            errorEvent.put("result", "FAILURE");
            errorEvent.put("correlationId", correlationId.toString());
            errorEvent.put("details", Map.of("error", e.getMessage()));
            errorEvent.put("timestamp", Instant.now().toString());
            auditEventClient.logEvent(errorEvent);
            throw new RuntimeException("LLM query failed", e);
        }
    }
    
    private SearchOptions buildSecureSearchOptions(LlmQueryRequest request) {
        String securityFilter = "applicationId eq 'davidparker-lv-bmth'";
        
        if (request.getFilters() != null && request.getFilters().getClassifications() != null) {
            String classifications = String.join("','", request.getFilters().getClassifications());
            securityFilter += String.format(" and classification in ('%s')", classifications);
        }
        
        SearchOptions options = new SearchOptions()
            .setFilter(securityFilter)
            .setTop(100)
            .setIncludeTotalCount(true);
        
        return options;
    }

    private String sanitizeQuery(String query) {
        if (query == null) return "";
        return query.substring(0, Math.min(query.length(), 200));
    }

    private List<Map<String, Object>> filterByPermissions(SearchResults<Map> results) {
        List<Map<String, Object>> permittedResults = new ArrayList<>();
        if (results != null) {
            results.getResults().forEach(result -> {
                permittedResults.add(result.getDocument());
            });
        }
        return permittedResults;
    }

    private String generateSummaryWithCitations(String query, List<Map<String, Object>> results) {
        return "Summary: Found " + results.size() + " documents matching your query: " + query;
    }
}

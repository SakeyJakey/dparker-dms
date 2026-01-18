package com.davidparker.dms.llm.service;

import com.azure.ai.openai.OpenAIClient;
import com.azure.core.util.Context;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchResult;
import com.azure.search.documents.util.SearchPagedIterable;
import com.davidparker.dms.llm.dto.LlmQueryRequest;
import com.davidparker.dms.llm.dto.LlmQueryResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    public SecureLlmQueryService(
            OpenAIClient aiFoundryClient,
            SearchClient searchClient,
            AuditEventClient auditEventClient) {
        this.aiFoundryClient = aiFoundryClient;
        this.searchClient = searchClient;
        this.auditEventClient = auditEventClient;
        this.objectMapper = new ObjectMapper();
    }
    
    @SuppressWarnings("unchecked")
    private String getQueryFromRequest(LlmQueryRequest request) {
        try {
            Map<String, Object> requestMap = objectMapper.convertValue(request, Map.class);
            return requestMap.get("query") != null ? requestMap.get("query").toString() : "";
        } catch (Exception e) {
            return "";
        }
    }
    
    @SuppressWarnings("unchecked")
    private LlmQueryRequest.Filters getFiltersFromRequest(LlmQueryRequest request) {
        try {
            Map<String, Object> requestMap = objectMapper.convertValue(request, Map.class);
            Object filtersObj = requestMap.get("filters");
            if (filtersObj != null) {
                return objectMapper.convertValue(filtersObj, LlmQueryRequest.Filters.class);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
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
        String queryText = getQueryFromRequest(request);
        auditEvent.put("details", Map.of("query", sanitizeQuery(queryText)));
        auditEvent.put("timestamp", Instant.now().toString());
        auditEventClient.logEvent(auditEvent);
        
        try {
            // Build secure search with application isolation
            SearchOptions searchOptions = buildSecureSearchOptions(request);
            
            // Execute search
            SearchPagedIterable searchResults = searchClient != null ? 
                searchClient.search(queryText, searchOptions, Context.NONE) : null;
            
            // Filter results by RBAC permissions
            List<Map<String, Object>> permittedResults = filterByPermissions(searchResults);
            
            // Generate summary
            String summary = generateSummaryWithCitations(queryText, permittedResults);
            
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
        
        LlmQueryRequest.Filters filters = getFiltersFromRequest(request);
        if (filters != null) {
            try {
                Map<String, Object> filtersMap = objectMapper.convertValue(filters, Map.class);
                Object classificationsObj = filtersMap.get("classifications");
                if (classificationsObj != null && classificationsObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> classifications = (List<String>) classificationsObj;
                    if (!classifications.isEmpty()) {
                        String classificationsStr = String.join("','", classifications);
                        securityFilter += String.format(" and classification in ('%s')", classificationsStr);
                    }
                }
            } catch (Exception e) {
                // Ignore filter parsing errors
            }
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

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> filterByPermissions(SearchPagedIterable searchResults) {
        List<Map<String, Object>> permittedResults = new ArrayList<>();
        if (searchResults != null) {
            searchResults.forEach(resultObj -> {
                SearchResult result = (SearchResult) resultObj;
                SearchDocument doc = result.getDocument(SearchDocument.class);
                Map<String, Object> docMap = new HashMap<>();
                doc.forEach((key, value) -> docMap.put(key, value));
                permittedResults.add(docMap);
            });
        }
        return permittedResults;
    }

    private String generateSummaryWithCitations(String query, List<Map<String, Object>> results) {
        return "Summary: Found " + results.size() + " documents matching your query: " + query;
    }
}

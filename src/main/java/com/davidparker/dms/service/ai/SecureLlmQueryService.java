package com.davidparker.dms.service.ai;

import com.azure.ai.openai.OpenAIClient;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchResults;
import com.azure.search.documents.models.VectorizedQuery;
import com.azure.search.documents.models.VectorSearchOptions;
import com.davidparker.dms.model.AuditEvent;
import com.davidparker.dms.model.RegisteredApplication;
import com.davidparker.dms.service.ApplicationContext;
import com.davidparker.dms.service.AuditService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SecureLlmQueryService {

    private final OpenAIClient aiFoundryClient;
    private final SearchClient searchClient;
    private final AuditService auditService;

    public SecureLlmQueryService(
            OpenAIClient aiFoundryClient,
            SearchClient searchClient,
            AuditService auditService) {
        this.aiFoundryClient = aiFoundryClient;
        this.searchClient = searchClient;
        this.auditService = auditService;
    }

    @PreAuthorize("hasRole('DMS.LLM.Service') or hasPermission(#request.applicationId, 'LLM_QUERY')")
    public LlmQueryResponse executeQuery(LlmQueryRequest request) {
        UUID correlationId = UUID.randomUUID();
        
        // 1. Log query initiation
        auditService.logEvent(AuditEvent.builder()
            .eventType(AuditEvent.EventType.LLM_QUERY_INITIATED)
            .eventCategory(AuditEvent.EventCategory.LLM_QUERIES)
            .action("LLM_QUERY_INITIATED")
            .result(AuditEvent.AuditResult.SUCCESS)
            .correlationId(correlationId)
            .details(Map.of("query", sanitizeQuery(request.getQuery())))
            .build());
        
        try {
            // 2. Use LLM to understand query and extract search parameters
            QueryIntent intent = analyzeQueryIntent(request.getQuery());
            
            // 3. Build secure search with application isolation
            SearchOptions searchOptions = buildSecureSearchOptions(intent, request);
            
            // 4. Execute vector + keyword hybrid search
            SearchResults<SearchDocument> results = searchClient.search(
                intent.getKeywords(),
                searchOptions,
                java.util.Map.class
            );
            
            // 5. Filter results by RBAC permissions
            List<DocumentResult> permittedResults = filterByPermissions(results, request);
            
            // 6. Generate LLM summary with citations
            String summary = generateSummaryWithCitations(request.getQuery(), permittedResults);
            
            // 7. Log query completion with result metadata
            auditService.logEvent(AuditEvent.builder()
                .eventType(AuditEvent.EventType.LLM_QUERY_COMPLETED)
                .eventCategory(AuditEvent.EventCategory.LLM_QUERIES)
                .action("LLM_QUERY_COMPLETED")
                .result(AuditEvent.AuditResult.SUCCESS)
                .correlationId(correlationId)
                .details(Map.of(
                    "resultCount", String.valueOf(permittedResults.size()),
                    "documentsAccessed", permittedResults.stream()
                        .map(DocumentResult::getDocumentId)
                        .map(UUID::toString)
                        .reduce((a, b) -> a + "," + b)
                        .orElse("")
                ))
                .build());
            
            return LlmQueryResponse.builder()
                .correlationId(correlationId)
                .summary(summary)
                .results(permittedResults)
                .queryMetadata(intent)
                .build();
        } catch (Exception e) {
            auditService.logEvent(AuditEvent.builder()
                .eventType(AuditEvent.EventType.LLM_QUERY_COMPLETED)
                .eventCategory(AuditEvent.EventCategory.LLM_QUERIES)
                .action("LLM_QUERY_FAILED")
                .result(AuditEvent.AuditResult.FAILURE)
                .correlationId(correlationId)
                .details(Map.of("error", e.getMessage()))
                .build());
            throw new RuntimeException("LLM query failed", e);
        }
    }
    
    private SearchOptions buildSecureSearchOptions(QueryIntent intent, LlmQueryRequest request) {
        // Get current application context
        RegisteredApplication app = ApplicationContext.getCurrent();
        
        // Build filter to enforce application isolation and classification
        String securityFilter = String.format(
            "applicationId eq '%s'",
            app != null ? app.getId().toString() : ""
        );
        
        if (request.getFilters() != null && request.getFilters().getClassifications() != null) {
            String classifications = String.join("','", request.getFilters().getClassifications());
            securityFilter += String.format(" and classification in ('%s')", classifications);
        }
        
        // Add intent-based filters
        if (intent.getDateRange() != null) {
            securityFilter += String.format(" and createdAt ge %s and createdAt le %s",
                intent.getDateRange().getStart(),
                intent.getDateRange().getEnd());
        }
        
        SearchOptions options = new SearchOptions()
            .setFilter(securityFilter)
            .setTop(100)
            .setIncludeTotalCount(true);
        
        if (intent.getQueryEmbedding() != null) {
            options.setVectorSearchOptions(new VectorSearchOptions()
                .setQueries(new VectorizedQuery(intent.getQueryEmbedding())
                    .setKNearestNeighborsCount(50)
                    .setFields("contentVector")));
        }
        
        return options;
    }

    private String sanitizeQuery(String query) {
        if (query == null) return "";
        // Remove potentially sensitive information
        return query.substring(0, Math.min(query.length(), 200));
    }

    private QueryIntent analyzeQueryIntent(String query) {
        // TODO: Use LLM to analyze query intent
        QueryIntent intent = new QueryIntent();
        intent.setKeywords(query);
        return intent;
    }

    private List<DocumentResult> filterByPermissions(SearchResults<SearchDocument> results, LlmQueryRequest request) {
        // TODO: Implement RBAC filtering
        List<DocumentResult> permittedResults = new ArrayList<>();
        results.getResults().forEach(result -> {
            DocumentResult docResult = new DocumentResult();
            docResult.setDocumentId(UUID.fromString((String) result.getDocument().get("documentId")));
            docResult.setContent((String) result.getDocument().get("content"));
            permittedResults.add(docResult);
        });
        return permittedResults;
    }

    private String generateSummaryWithCitations(String query, List<DocumentResult> results) {
        // TODO: Use LLM to generate summary with citations
        return "Summary: Found " + results.size() + " documents matching your query.";
    }

    public static class LlmQueryRequest {
        private String query;
        private Filters filters;
        private Integer maxResults;
        private Boolean includeSummary;
        private UUID applicationId;

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public Filters getFilters() {
            return filters;
        }

        public void setFilters(Filters filters) {
            this.filters = filters;
        }

        public Integer getMaxResults() {
            return maxResults;
        }

        public void setMaxResults(Integer maxResults) {
            this.maxResults = maxResults;
        }

        public Boolean getIncludeSummary() {
            return includeSummary;
        }

        public void setIncludeSummary(Boolean includeSummary) {
            this.includeSummary = includeSummary;
        }

        public UUID getApplicationId() {
            return applicationId;
        }

        public void setApplicationId(UUID applicationId) {
            this.applicationId = applicationId;
        }
    }

    public static class Filters {
        private List<String> classifications;
        private DateRange dateRange;

        public List<String> getClassifications() {
            return classifications;
        }

        public void setClassifications(List<String> classifications) {
            this.classifications = classifications;
        }

        public DateRange getDateRange() {
            return dateRange;
        }

        public void setDateRange(DateRange dateRange) {
            this.dateRange = dateRange;
        }
    }

    public static class DateRange {
        private String start;
        private String end;

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }
    }

    public static class LlmQueryResponse {
        private UUID correlationId;
        private String summary;
        private List<DocumentResult> results;
        private QueryIntent queryMetadata;

        public static Builder builder() {
            return new Builder();
        }

        public UUID getCorrelationId() {
            return correlationId;
        }

        public String getSummary() {
            return summary;
        }

        public List<DocumentResult> getResults() {
            return results;
        }

        public QueryIntent getQueryMetadata() {
            return queryMetadata;
        }

        public static class Builder {
            private LlmQueryResponse response = new LlmQueryResponse();

            public Builder correlationId(UUID correlationId) {
                response.correlationId = correlationId;
                return this;
            }

            public Builder summary(String summary) {
                response.summary = summary;
                return this;
            }

            public Builder results(List<DocumentResult> results) {
                response.results = results;
                return this;
            }

            public Builder queryMetadata(QueryIntent queryMetadata) {
                response.queryMetadata = queryMetadata;
                return this;
            }

            public LlmQueryResponse build() {
                return response;
            }
        }
    }

    public static class DocumentResult {
        private UUID documentId;
        private String content;

        public UUID getDocumentId() {
            return documentId;
        }

        public void setDocumentId(UUID documentId) {
            this.documentId = documentId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public static class QueryIntent {
        private String keywords;
        private List<Double> queryEmbedding;
        private DateRange dateRange;

        public String getKeywords() {
            return keywords;
        }

        public void setKeywords(String keywords) {
            this.keywords = keywords;
        }

        public List<Double> getQueryEmbedding() {
            return queryEmbedding;
        }

        public void setQueryEmbedding(List<Double> queryEmbedding) {
            this.queryEmbedding = queryEmbedding;
        }

        public DateRange getDateRange() {
            return dateRange;
        }

        public void setDateRange(DateRange dateRange) {
            this.dateRange = dateRange;
        }
    }
}

package com.davidparker.dms.llm.service;

import com.azure.ai.openai.OpenAIClient;
import com.azure.search.documents.SearchClient;
import com.davidparker.dms.llm.dto.LlmQueryRequest;
import com.davidparker.dms.llm.dto.LlmQueryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecureLlmQueryServiceExpandedTest {

    @Mock
    private OpenAIClient aiFoundryClient;

    @Mock
    private AuditEventClient auditEventClient;

    @BeforeEach
    void setUp() {
        lenient().doNothing().when(auditEventClient).logEvent(any());
    }

    @Test
    void testQueryWithFilters() {
        SecureLlmQueryService service = new SecureLlmQueryService(
            aiFoundryClient, null, auditEventClient);

        LlmQueryRequest request = new LlmQueryRequest();
        request.setQuery("Find compliance documents");
        LlmQueryRequest.Filters filters = new LlmQueryRequest.Filters();
        filters.setClassifications(List.of("CONFIDENTIAL", "RESTRICTED"));
        request.setFilters(filters);

        LlmQueryResponse response = service.executeQuery(request);

        assertNotNull(response);
        assertNotNull(response.getCorrelationId());
        assertNotNull(response.getSummary());
        assertTrue(response.getSummary().contains("Find compliance documents"));
    }

    @Test
    void testQueryWithEmptyQuery() {
        SecureLlmQueryService service = new SecureLlmQueryService(
            aiFoundryClient, null, auditEventClient);

        LlmQueryRequest request = new LlmQueryRequest();
        request.setQuery("");

        LlmQueryResponse response = service.executeQuery(request);

        assertNotNull(response);
        assertEquals(0, response.getResults().size());
    }

    @Test
    void testQueryResultsAreEmptyWhenNoSearchClient() {
        SecureLlmQueryService service = new SecureLlmQueryService(
            aiFoundryClient, null, auditEventClient);

        LlmQueryRequest request = new LlmQueryRequest();
        request.setQuery("test query");

        LlmQueryResponse response = service.executeQuery(request);

        assertNotNull(response);
        assertTrue(response.getResults().isEmpty());
    }

    @Test
    void testQueryLogsAuditEvents() {
        SecureLlmQueryService service = new SecureLlmQueryService(
            aiFoundryClient, null, auditEventClient);

        LlmQueryRequest request = new LlmQueryRequest();
        request.setQuery("test");

        service.executeQuery(request);

        // Should log at least 2 events: initiation + completion
        verify(auditEventClient, atLeast(2)).logEvent(any());
    }

    @Test
    void testQueryWithMaxResults() {
        SecureLlmQueryService service = new SecureLlmQueryService(
            aiFoundryClient, null, auditEventClient);

        LlmQueryRequest request = new LlmQueryRequest();
        request.setQuery("test");
        request.setMaxResults(5);

        LlmQueryResponse response = service.executeQuery(request);

        assertNotNull(response);
    }

    @Test
    void testQuerySummaryContainsResultCount() {
        SecureLlmQueryService service = new SecureLlmQueryService(
            aiFoundryClient, null, auditEventClient);

        LlmQueryRequest request = new LlmQueryRequest();
        request.setQuery("Find documents");

        LlmQueryResponse response = service.executeQuery(request);

        assertTrue(response.getSummary().contains("0 documents"));
    }

    @Test
    void testQueryWithDateRangeFilter() {
        SecureLlmQueryService service = new SecureLlmQueryService(
            aiFoundryClient, null, auditEventClient);

        LlmQueryRequest request = new LlmQueryRequest();
        request.setQuery("quarterly reports");
        LlmQueryRequest.Filters filters = new LlmQueryRequest.Filters();
        LlmQueryRequest.DateRange dateRange = new LlmQueryRequest.DateRange();
        dateRange.setStart("2026-01-01");
        dateRange.setEnd("2026-03-31");
        filters.setDateRange(dateRange);
        request.setFilters(filters);

        LlmQueryResponse response = service.executeQuery(request);

        assertNotNull(response);
        assertNotNull(response.getCorrelationId());
    }

    @Test
    void testLlmQueryRequestDtoFields() {
        LlmQueryRequest request = new LlmQueryRequest();
        request.setQuery("test");
        request.setMaxResults(10);
        request.setIncludeSummary(true);
        request.setApplicationId(java.util.UUID.randomUUID());

        assertEquals("test", request.getQuery());
        assertEquals(10, request.getMaxResults());
        assertTrue(request.getIncludeSummary());
        assertNotNull(request.getApplicationId());
    }

    @Test
    void testLlmQueryResponseDtoFields() {
        LlmQueryResponse response = LlmQueryResponse.builder()
            .correlationId(java.util.UUID.randomUUID())
            .summary("test summary")
            .results(List.of())
            .totalCount(0)
            .build();

        assertNotNull(response.getCorrelationId());
        assertEquals("test summary", response.getSummary());
        assertTrue(response.getResults().isEmpty());
        assertEquals(0, response.getTotalCount());
    }
}

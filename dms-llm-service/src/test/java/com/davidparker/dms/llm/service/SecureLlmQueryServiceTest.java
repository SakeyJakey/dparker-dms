package com.davidparker.dms.llm.service;

import com.azure.ai.openai.OpenAIClient;
import com.azure.search.documents.SearchClient;
import com.davidparker.dms.llm.dto.LlmQueryRequest;
import com.davidparker.dms.llm.dto.LlmQueryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecureLlmQueryServiceTest {

    @Mock
    private OpenAIClient aiFoundryClient;

    @Mock
    private SearchClient searchClient;

    @Mock
    private AuditEventClient auditEventClient;

    @InjectMocks
    private SecureLlmQueryService secureLlmQueryService;

    private LlmQueryRequest request;

    @BeforeEach
    void setUp() {
        request = new LlmQueryRequest();
        request.setQuery("What documents contain PCI data?");
    }

    @Test
    void testExecuteQuery() {
        LlmQueryResponse response = secureLlmQueryService.executeQuery(request);

        assertNotNull(response);
        assertNotNull(response.getCorrelationId());
        assertNotNull(response.getSummary());
        verify(auditEventClient, atLeastOnce()).logEvent(any());
    }

    @Test
    void testExecuteQueryWithNullSearchClient() {
        SecureLlmQueryService service = new SecureLlmQueryService(
            aiFoundryClient, null, auditEventClient);

        LlmQueryResponse response = service.executeQuery(request);

        assertNotNull(response);
        assertNotNull(response.getCorrelationId());
        verify(auditEventClient, atLeastOnce()).logEvent(any());
    }
}

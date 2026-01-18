package com.davidparker.dms.llm.controller;

import com.davidparker.dms.llm.dto.LlmQueryRequest;
import com.davidparker.dms.llm.dto.LlmQueryResponse;
import com.davidparker.dms.llm.service.SecureLlmQueryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LlmQueryController.class)
class LlmQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SecureLlmQueryService llmQueryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "DMS.LLM.Service")
    void testExecuteQuery() throws Exception {
        LlmQueryRequest request = new LlmQueryRequest();
        request.setQuery("What documents contain PCI data?");

        LlmQueryResponse response = LlmQueryResponse.builder()
            .correlationId(UUID.randomUUID())
            .summary("Found 5 documents")
            .build();

        when(llmQueryService.executeQuery(any(LlmQueryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/llm/query")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.summary").value("Found 5 documents"));
    }

    @Test
    @WithMockUser(roles = "DMS.LLM.Service")
    void testComplianceCheck() throws Exception {
        LlmQueryRequest request = new LlmQueryRequest();
        request.setQuery("Check compliance for document");

        LlmQueryResponse response = LlmQueryResponse.builder()
            .correlationId(UUID.randomUUID())
            .summary("Compliance check completed")
            .build();

        when(llmQueryService.executeQuery(any(LlmQueryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/llm/compliance-check")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.summary").value("Compliance check completed"));
    }
}

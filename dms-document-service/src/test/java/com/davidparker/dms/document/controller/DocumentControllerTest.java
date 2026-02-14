package com.davidparker.dms.document.controller;

import com.davidparker.dms.document.model.Document;
import com.davidparker.dms.document.service.DocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DocumentController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {DocumentController.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.cloud.azure.keyvault.secret.enabled=false",
    "spring.cloud.azure.keyvault.secret.property-sources[0].enabled=false"
})
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    private final UUID documentId = UUID.randomUUID();
    private final UUID applicationId = UUID.randomUUID();

    private Document createTestDocument() {
        return Document.builder()
            .id(documentId)
            .applicationId(applicationId)
            .name("test-document.pdf")
            .classification(Document.Classification.INTERNAL)
            .version(1)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    }

    @Test
    void testListDocuments() throws Exception {
        Document doc = createTestDocument();
        Page<Document> page = new PageImpl<>(List.of(doc), PageRequest.of(0, 10), 1);
        when(documentService.listDocuments(eq(applicationId), isNull(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/documents")
                .param("applicationId", applicationId.toString())
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());

        verify(documentService).listDocuments(eq(applicationId), isNull(), any());
    }

    @Test
    void testListDocumentsWithClassification() throws Exception {
        Document doc = createTestDocument();
        doc.setClassification(Document.Classification.CONFIDENTIAL);
        Page<Document> page = new PageImpl<>(List.of(doc), PageRequest.of(0, 10), 1);
        when(documentService.listDocuments(eq(applicationId), eq(Document.Classification.CONFIDENTIAL), any()))
            .thenReturn(page);

        mockMvc.perform(get("/api/v1/documents")
                .param("applicationId", applicationId.toString())
                .param("classification", "CONFIDENTIAL")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk());

        verify(documentService).listDocuments(eq(applicationId), eq(Document.Classification.CONFIDENTIAL), any());
    }
}

package com.davidparker.dms.document.controller;

import com.davidparker.dms.document.model.Document;
import com.davidparker.dms.document.service.DocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID documentId = UUID.randomUUID();
    private UUID applicationId = UUID.randomUUID();

    @Test
    @WithMockUser(roles = {"DMS.davidparker-lv-bmth", "DMS.User"})
    void testListDocuments() throws Exception {
        Document document = Document.builder()
            .id(documentId)
            .applicationId(applicationId)
            .name("test-document.pdf")
            .classification(Document.Classification.INTERNAL)
            .version(1)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        Page<Document> documentPage = new PageImpl<>(List.of(document), PageRequest.of(0, 10), 1);

        when(documentService.listDocuments(eq(applicationId), isNull(), any())).thenReturn(documentPage);

        mockMvc.perform(get("/api/v1/documents")
                .param("applicationId", applicationId.toString())
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].name").value("test-document.pdf"));

        verify(documentService).listDocuments(eq(applicationId), isNull(), any());
    }

    @Test
    @WithMockUser(roles = {"DMS.davidparker-lv-bmth", "DMS.User"})
    void testListDocumentsWithClassification() throws Exception {
        Document document = Document.builder()
            .id(documentId)
            .applicationId(applicationId)
            .name("test-document.pdf")
            .classification(Document.Classification.CONFIDENTIAL)
            .version(1)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        Page<Document> documentPage = new PageImpl<>(List.of(document), PageRequest.of(0, 10), 1);

        when(documentService.listDocuments(eq(applicationId), eq(Document.Classification.CONFIDENTIAL), any()))
            .thenReturn(documentPage);

        mockMvc.perform(get("/api/v1/documents")
                .param("applicationId", applicationId.toString())
                .param("classification", "CONFIDENTIAL")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].classification").value("CONFIDENTIAL"));

        verify(documentService).listDocuments(eq(applicationId), eq(Document.Classification.CONFIDENTIAL), any());
    }

    @Test
    @WithMockUser(roles = {"DMS.davidparker-lv-bmth", "DMS.User"})
    void testUploadDocument() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.pdf", "application/pdf", "test content".getBytes());

        Document document = Document.builder()
            .id(documentId)
            .applicationId(applicationId)
            .name("test.pdf")
            .classification(Document.Classification.INTERNAL)
            .version(1)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .claim("dms_application_id", applicationId.toString())
            .build();
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);

        when(documentService.uploadDocument(eq(applicationId), any(), eq("test.pdf"), 
            eq(Document.Classification.INTERNAL))).thenReturn(document);

        mockMvc.perform(multipart("/api/v1/documents")
                .file(file)
                .param("name", "test.pdf")
                .param("classification", "INTERNAL")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("test.pdf"));

        verify(documentService).uploadDocument(eq(applicationId), any(), eq("test.pdf"), 
            eq(Document.Classification.INTERNAL));
    }

    @Test
    @WithMockUser(roles = {"DMS.davidparker-lv-bmth", "DMS.User"})
    void testGetDocument() throws Exception {
        Document document = Document.builder()
            .id(documentId)
            .applicationId(applicationId)
            .name("test-document.pdf")
            .classification(Document.Classification.INTERNAL)
            .version(1)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .claim("dms_application_id", applicationId.toString())
            .build();

        when(documentService.getDocument(documentId, applicationId)).thenReturn(document);

        mockMvc.perform(get("/api/v1/documents/{id}", documentId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(documentId.toString()))
            .andExpect(jsonPath("$.name").value("test-document.pdf"));

        verify(documentService).getDocument(documentId, applicationId);
    }

    @Test
    @WithMockUser(roles = {"DMS.davidparker-lv-bmth", "DMS.User"})
    void testUpdateDocument() throws Exception {
        Document updatedDocument = Document.builder()
            .id(documentId)
            .applicationId(applicationId)
            .name("updated-document.pdf")
            .classification(Document.Classification.CONFIDENTIAL)
            .version(1)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .claim("dms_application_id", applicationId.toString())
            .build();

        when(documentService.updateDocument(eq(documentId), eq(applicationId), any(Document.class)))
            .thenReturn(updatedDocument);

        mockMvc.perform(put("/api/v1/documents/{id}", documentId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDocument)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("updated-document.pdf"));

        verify(documentService).updateDocument(eq(documentId), eq(applicationId), any(Document.class));
    }

    @Test
    @WithMockUser(roles = {"DMS.davidparker-lv-bmth", "DMS.User"})
    void testDeleteDocument() throws Exception {
        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .claim("dms_application_id", applicationId.toString())
            .build();

        doNothing().when(documentService).deleteDocument(documentId, applicationId);

        mockMvc.perform(delete("/api/v1/documents/{id}", documentId)
                .with(csrf()))
            .andExpect(status().isNoContent());

        verify(documentService).deleteDocument(documentId, applicationId);
    }

    @Test
    @WithMockUser(roles = {"DMS.davidparker-lv-bmth", "DMS.User"})
    void testDownloadDocument() throws Exception {
        byte[] content = "test content".getBytes();

        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .claim("dms_application_id", applicationId.toString())
            .build();

        when(documentService.downloadDocument(documentId, applicationId)).thenReturn(content);

        mockMvc.perform(get("/api/v1/documents/{id}/download", documentId))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"" + documentId + "\""))
            .andExpect(content().bytes(content));

        verify(documentService).downloadDocument(documentId, applicationId);
    }
}

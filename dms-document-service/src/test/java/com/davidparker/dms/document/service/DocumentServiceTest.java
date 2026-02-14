package com.davidparker.dms.document.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.davidparker.dms.document.model.Document;
import com.davidparker.dms.document.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private BlobServiceClient blobServiceClient;

    @Mock
    private AuditEventClient auditEventClient;

    @Mock
    private ApplicationServiceClient applicationServiceClient;

    @Mock
    private BlobContainerClient blobContainerClient;

    @Mock
    private BlobClient blobClient;

    @InjectMocks
    private DocumentService documentService;

    private UUID documentId;
    private UUID applicationId;
    private Document document;

    @BeforeEach
    void setUp() {
        documentId = UUID.randomUUID();
        applicationId = UUID.randomUUID();
        document = Document.builder()
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
    void testListDocuments() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Document> documentPage = new PageImpl<>(List.of(document), pageRequest, 1);

        when(documentRepository.findByApplicationId(applicationId, pageRequest)).thenReturn(documentPage);

        Page<Document> result = documentService.listDocuments(applicationId, null, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(document.getName(), result.getContent().get(0).getName());
        verify(documentRepository).findByApplicationId(applicationId, pageRequest);
    }

    @Test
    void testListDocumentsWithClassification() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Document> documentPage = new PageImpl<>(List.of(document), pageRequest, 1);

        when(documentRepository.findByApplicationIdAndClassification(
            applicationId, Document.Classification.INTERNAL, pageRequest)).thenReturn(documentPage);

        Page<Document> result = documentService.listDocuments(
            applicationId, Document.Classification.INTERNAL, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(documentRepository).findByApplicationIdAndClassification(
            applicationId, Document.Classification.INTERNAL, pageRequest);
    }

    @Test
    void testUploadDocument() throws Exception {
        MultipartFile file = new MockMultipartFile(
            "file", "test.pdf", "application/pdf", "test content".getBytes());

        java.util.Map<String, Object> appResponse = new java.util.HashMap<>();
        appResponse.put("id", applicationId.toString());
        when(applicationServiceClient.getApplication(applicationId))
            .thenReturn(Mono.just(appResponse));
        when(documentRepository.save(any(Document.class))).thenReturn(document);
        when(blobServiceClient.getBlobContainerClient(anyString())).thenReturn(blobContainerClient);
        when(blobContainerClient.getBlobClient(anyString())).thenReturn(blobClient);
        doNothing().when(blobClient).upload(any(InputStream.class), anyLong(), eq(true));
        when(blobClient.getBlobUrl()).thenReturn("https://storage.blob.core.windows.net/container/blob");

        Document result = documentService.uploadDocument(applicationId, file, "test.pdf", 
            Document.Classification.INTERNAL);

        assertNotNull(result);
        verify(documentRepository, times(2)).save(any(Document.class));
        verify(blobClient).upload(any(InputStream.class), anyLong(), eq(true));
        verify(auditEventClient).logEvent(any());
    }

    @Test
    void testUploadDocumentApplicationNotFound() {
        MultipartFile file = new MockMultipartFile(
            "file", "test.pdf", "application/pdf", "test content".getBytes());

        when(applicationServiceClient.getApplication(applicationId))
            .thenReturn(Mono.empty());

        assertThrows(RuntimeException.class, () -> {
            documentService.uploadDocument(applicationId, file, "test.pdf", 
                Document.Classification.INTERNAL);
        });
    }

    @Test
    void testGetDocument() {
        when(documentRepository.findByIdAndApplicationId(documentId, applicationId))
            .thenReturn(List.of(document));

        Document result = documentService.getDocument(documentId, applicationId);

        assertNotNull(result);
        assertEquals(documentId, result.getId());
        verify(documentRepository).findByIdAndApplicationId(documentId, applicationId);
        verify(auditEventClient).logEvent(any());
    }

    @Test
    void testGetDocumentNotFound() {
        when(documentRepository.findByIdAndApplicationId(documentId, applicationId))
            .thenReturn(List.of());

        assertThrows(RuntimeException.class, () -> {
            documentService.getDocument(documentId, applicationId);
        });
    }

    @Test
    void testUpdateDocument() {
        Document updatedDocument = Document.builder()
            .name("updated-document.pdf")
            .classification(Document.Classification.CONFIDENTIAL)
            .pciRelevant(true)
            .build();

        when(documentRepository.findByIdAndApplicationId(documentId, applicationId))
            .thenReturn(List.of(document));
        when(documentRepository.save(any(Document.class))).thenReturn(document);

        Document result = documentService.updateDocument(documentId, applicationId, updatedDocument);

        assertNotNull(result);
        verify(documentRepository).save(any(Document.class));
        verify(auditEventClient).logEvent(any());
    }

    @Test
    void testDeleteDocument() {
        when(documentRepository.findByIdAndApplicationId(documentId, applicationId))
            .thenReturn(List.of(document));
        doNothing().when(documentRepository).delete(any(Document.class));

        documentService.deleteDocument(documentId, applicationId);

        verify(documentRepository).delete(any(Document.class));
        verify(auditEventClient).logEvent(any());
    }

    @Test
    void testDownloadDocument() {
        byte[] content = "test content".getBytes();

        when(documentRepository.findByIdAndApplicationId(documentId, applicationId))
            .thenReturn(List.of(document));
        when(blobServiceClient.getBlobContainerClient(anyString())).thenReturn(blobContainerClient);
        when(blobContainerClient.getBlobClient(anyString())).thenReturn(blobClient);
        when(blobClient.downloadContent()).thenReturn(
            com.azure.core.util.BinaryData.fromBytes(content));

        byte[] result = documentService.downloadDocument(documentId, applicationId);

        assertNotNull(result);
        assertArrayEquals(content, result);
        verify(auditEventClient).logEvent(any());
    }

    @Test
    void testDownloadDocumentNotFound() {
        when(documentRepository.findByIdAndApplicationId(documentId, applicationId))
            .thenReturn(List.of());

        assertThrows(RuntimeException.class, () -> {
            documentService.downloadDocument(documentId, applicationId);
        });
    }
}

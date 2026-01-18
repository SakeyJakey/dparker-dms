package com.davidparker.dms.service.ai;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.models.Embeddings;
import com.azure.ai.openai.models.EmbeddingsOptions;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.models.SearchDocument;
import com.davidparker.dms.model.AuditEvent;
import com.davidparker.dms.model.Document;
import com.davidparker.dms.service.AuditService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DocumentEmbeddingService {

    private final OpenAIClient openAIClient;
    private final SearchClient searchClient;
    private final AuditService auditService;

    public DocumentEmbeddingService(
            OpenAIClient openAIClient,
            SearchClient searchClient,
            AuditService auditService) {
        this.openAIClient = openAIClient;
        this.searchClient = searchClient;
        this.auditService = auditService;
    }

    @Async
    @EventListener
    public void onDocumentUploaded(DocumentUploadedEvent event) {
        Document document = event.getDocument();
        
        try {
            // 1. Extract text content (would need actual text extraction from file)
            String textContent = extractText(document);
            
            // 2. Chunk document for embedding
            List<DocumentChunk> chunks = chunkDocument(textContent, document.getId());
            
            // 3. Generate embeddings via Azure OpenAI
            for (DocumentChunk chunk : chunks) {
                EmbeddingsOptions options = new EmbeddingsOptions(List.of(chunk.getContent()));
                Embeddings embeddings = openAIClient.getEmbeddings("text-embedding-ada-002", options);
                if (embeddings != null && embeddings.getData() != null && !embeddings.getData().isEmpty()) {
                    chunk.setEmbedding(embeddings.getData().get(0).getEmbedding());
                }
            }
            
            // 4. Index in Azure AI Search with application scope
            indexDocumentChunks(chunks, document);
            
            // 5. Audit the indexing operation
            auditService.logEvent(AuditEvent.builder()
                .eventType(AuditEvent.EventType.DOCUMENT_INDEXED_FOR_LLM)
                .eventCategory(AuditEvent.EventCategory.LLM_QUERIES)
                .action("DOCUMENT_INDEXED")
                .result(AuditEvent.AuditResult.SUCCESS)
                .resourceId(document.getId())
                .resourceName(document.getName())
                .details(Map.of("chunkCount", String.valueOf(chunks.size())))
                .build());
        } catch (Exception e) {
            // Log error but don't fail the upload
            auditService.logEvent(AuditEvent.builder()
                .eventType(AuditEvent.EventType.DOCUMENT_INDEXED_FOR_LLM)
                .eventCategory(AuditEvent.EventCategory.LLM_QUERIES)
                .action("DOCUMENT_INDEX_FAILED")
                .result(AuditEvent.AuditResult.FAILURE)
                .resourceId(document.getId())
                .details(Map.of("error", e.getMessage()))
                .build());
        }
    }
    
    private String extractText(Document document) {
        // TODO: Implement actual text extraction from document file
        // This would use Apache Tika or similar library
        return "Sample extracted text from document: " + document.getName();
    }
    
    private List<DocumentChunk> chunkDocument(String textContent, java.util.UUID documentId) {
        // Simple chunking - in production, use more sophisticated chunking
        int chunkSize = 1000;
        List<DocumentChunk> chunks = new ArrayList<>();
        
        for (int i = 0; i < textContent.length(); i += chunkSize) {
            int end = Math.min(i + chunkSize, textContent.length());
            String chunkText = textContent.substring(i, end);
            
            DocumentChunk chunk = new DocumentChunk();
            chunk.setId(java.util.UUID.randomUUID());
            chunk.setDocumentId(documentId);
            chunk.setContent(chunkText);
            chunk.setChunkIndex(i / chunkSize);
            chunks.add(chunk);
        }
        
        return chunks;
    }
    
    private void indexDocumentChunks(List<DocumentChunk> chunks, Document document) {
        List<SearchDocument> searchDocs = chunks.stream()
            .map(chunk -> {
                SearchDocument doc = new SearchDocument();
                doc.put("id", chunk.getId().toString());
                doc.put("documentId", document.getId().toString());
                doc.put("applicationId", document.getApplication().getId().toString());
                doc.put("content", chunk.getContent());
                if (chunk.getEmbedding() != null) {
                    doc.put("contentVector", chunk.getEmbedding());
                }
                doc.put("classification", document.getClassification().name());
                doc.put("createdAt", document.getCreatedAt().toString());
                return doc;
            })
            .toList();
            
        searchClient.uploadDocuments(searchDocs);
    }

    public static class DocumentUploadedEvent {
        private final Document document;

        public DocumentUploadedEvent(Document document) {
            this.document = document;
        }

        public Document getDocument() {
            return document;
        }
    }

    public static class DocumentChunk {
        private java.util.UUID id;
        private java.util.UUID documentId;
        private String content;
        private List<Double> embedding;
        private int chunkIndex;

        public java.util.UUID getId() {
            return id;
        }

        public void setId(java.util.UUID id) {
            this.id = id;
        }

        public java.util.UUID getDocumentId() {
            return documentId;
        }

        public void setDocumentId(java.util.UUID documentId) {
            this.documentId = documentId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public List<Double> getEmbedding() {
            return embedding;
        }

        public void setEmbedding(List<Double> embedding) {
            this.embedding = embedding;
        }

        public int getChunkIndex() {
            return chunkIndex;
        }

        public void setChunkIndex(int chunkIndex) {
            this.chunkIndex = chunkIndex;
        }
    }
}

package com.davidparker.dms.document.service;

import com.davidparker.dms.document.exception.ResourceNotFoundException;
import com.davidparker.dms.document.model.Document;
import com.davidparker.dms.document.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Service
public class PreviewService {

    private final DocumentRepository documentRepository;

    public PreviewService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public record PreviewResult(byte[] data, String contentType) {}

    public PreviewResult generatePreview(UUID documentId, int page) {
        Document doc = documentRepository.findById(documentId)
            .orElseThrow(() -> new ResourceNotFoundException("Document", documentId.toString()));

        String name = doc.getName().toLowerCase();
        String contentType;

        if (name.endsWith(".pdf")) {
            contentType = "application/pdf";
        } else if (name.endsWith(".png")) {
            contentType = "image/png";
        } else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            contentType = "image/jpeg";
        } else if (name.endsWith(".txt") || name.endsWith(".csv")) {
            contentType = "text/plain";
        } else {
            contentType = "text/html";
        }

        String html = String.format(
            "<html><body><h2>Preview: %s</h2><p>Classification: %s</p><p>Version: %d</p><p>Page: %d</p><p>Preview not available for this file type. Please download the document.</p></body></html>",
            doc.getName(), doc.getClassification(), doc.getVersion(), page);

        return new PreviewResult(html.getBytes(StandardCharsets.UTF_8), "text/html");
    }

    public Map<String, Object> getPreviewInfo(UUID documentId) {
        Document doc = documentRepository.findById(documentId)
            .orElseThrow(() -> new ResourceNotFoundException("Document", documentId.toString()));

        String name = doc.getName().toLowerCase();
        boolean previewable = name.endsWith(".pdf") || name.endsWith(".txt") ||
            name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") ||
            name.endsWith(".html") || name.endsWith(".csv");

        return Map.of(
            "documentId", documentId.toString(),
            "name", doc.getName(),
            "previewable", previewable,
            "pages", 1,
            "contentType", getContentType(name)
        );
    }

    private String getContentType(String name) {
        if (name.endsWith(".pdf")) return "application/pdf";
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".txt")) return "text/plain";
        return "text/html";
    }
}

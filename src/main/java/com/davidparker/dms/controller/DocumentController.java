package com.davidparker.dms.controller;

import com.davidparker.dms.model.Document;
import com.davidparker.dms.service.DocumentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DMS.davidparker-lv-bmth', 'DMS.User')")
    public ResponseEntity<Page<Document>> listDocuments(
            @RequestParam(required = false) Document.Classification classification,
            Pageable pageable) {
        Page<Document> documents = documentService.listDocuments(classification, pageable);
        return ResponseEntity.ok(documents);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DMS.davidparker-lv-bmth', 'DMS.User')")
    public ResponseEntity<Document> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("classification") Document.Classification classification) {
        Document document = documentService.uploadDocument(file, name, classification);
        return ResponseEntity.ok(document);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DMS.davidparker-lv-bmth', 'DMS.User')")
    public ResponseEntity<Document> getDocument(@PathVariable UUID id) {
        Document document = documentService.getDocument(id);
        return ResponseEntity.ok(document);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DMS.davidparker-lv-bmth', 'DMS.User')")
    public ResponseEntity<Document> updateDocument(
            @PathVariable UUID id,
            @RequestBody Document document) {
        Document updated = documentService.updateDocument(id, document);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DMS.davidparker-lv-bmth', 'DMS.User')")
    public ResponseEntity<Void> deleteDocument(@PathVariable UUID id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('DMS.davidparker-lv-bmth', 'DMS.User')")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable UUID id) {
        byte[] content = documentService.downloadDocument(id);
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"" + id + "\"")
            .body(content);
    }
}

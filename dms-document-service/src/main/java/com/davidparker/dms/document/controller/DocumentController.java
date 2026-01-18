package com.davidparker.dms.document.controller;

import com.davidparker.dms.document.model.Document;
import com.davidparker.dms.document.service.DocumentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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
            @RequestParam UUID applicationId,
            @RequestParam(required = false) Document.Classification classification,
            Pageable pageable) {
        Page<Document> documents = documentService.listDocuments(applicationId, classification, pageable);
        return ResponseEntity.ok(documents);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DMS.davidparker-lv-bmth', 'DMS.User')")
    public ResponseEntity<Document> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("classification") Document.Classification classification,
            JwtAuthenticationToken authentication) {
        
        UUID applicationId = extractApplicationId(authentication);
        Document document = documentService.uploadDocument(applicationId, file, name, classification);
        return ResponseEntity.ok(document);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DMS.davidparker-lv-bmth', 'DMS.User')")
    public ResponseEntity<Document> getDocument(
            @PathVariable UUID id,
            JwtAuthenticationToken authentication) {
        UUID applicationId = extractApplicationId(authentication);
        Document document = documentService.getDocument(id, applicationId);
        return ResponseEntity.ok(document);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DMS.davidparker-lv-bmth', 'DMS.User')")
    public ResponseEntity<Document> updateDocument(
            @PathVariable UUID id,
            @RequestBody Document document,
            JwtAuthenticationToken authentication) {
        UUID applicationId = extractApplicationId(authentication);
        Document updated = documentService.updateDocument(id, applicationId, document);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DMS.davidparker-lv-bmth', 'DMS.User')")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable UUID id,
            JwtAuthenticationToken authentication) {
        UUID applicationId = extractApplicationId(authentication);
        documentService.deleteDocument(id, applicationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('DMS.davidparker-lv-bmth', 'DMS.User')")
    public ResponseEntity<byte[]> downloadDocument(
            @PathVariable UUID id,
            JwtAuthenticationToken authentication) {
        UUID applicationId = extractApplicationId(authentication);
        byte[] content = documentService.downloadDocument(id, applicationId);
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"" + id + "\"")
            .body(content);
    }

    private UUID extractApplicationId(JwtAuthenticationToken authentication) {
        Jwt jwt = authentication.getToken();
        // Extract application ID from JWT claims or call admin service
        // For now, return a default - in production, this would be from JWT claims
        String appId = jwt.getClaimAsString("dms_application_id");
        if (appId != null) {
            return UUID.fromString(appId);
        }
        throw new SecurityException("Application ID not found in token");
    }
}

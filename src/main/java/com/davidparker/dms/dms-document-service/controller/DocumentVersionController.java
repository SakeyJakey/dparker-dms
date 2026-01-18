package com.davidparker.dms.dms_document_service.controller;

import com.davidparker.dms.dms_document_service.dto.DocumentVersionResponse;
import com.davidparker.dms.dms_document_service.service.DocumentVersionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents/{id}/versions")
@PreAuthorize("hasAnyRole('DMS.davidparker-lv-bmth', 'DMS.User')")
public class DocumentVersionController {

    private final DocumentVersionService documentVersionService;

    public DocumentVersionController(DocumentVersionService documentVersionService) {
        this.documentVersionService = documentVersionService;
    }

    @GetMapping
    public ResponseEntity<List<DocumentVersionResponse>> getDocumentVersions(@PathVariable UUID id) {
        return ResponseEntity.ok(documentVersionService.getDocumentVersions(id));
    }

    @GetMapping("/{version}")
    public ResponseEntity<DocumentVersionResponse> getDocumentVersion(
            @PathVariable UUID id,
            @PathVariable Integer version) {
        return ResponseEntity.ok(documentVersionService.getDocumentVersion(id, version));
    }
}

package com.davidparker.dms.document.controller;

import com.davidparker.dms.document.service.PreviewService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents/{documentId}/preview")
public class PreviewController {

    private final PreviewService previewService;

    public PreviewController(PreviewService previewService) {
        this.previewService = previewService;
    }

    @GetMapping
    public ResponseEntity<byte[]> getPreview(@PathVariable UUID documentId,
                                              @RequestParam(defaultValue = "1") int page) {
        PreviewService.PreviewResult result = previewService.generatePreview(documentId, page);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(result.contentType()))
            .body(result.data());
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getPreviewInfo(@PathVariable UUID documentId) {
        return ResponseEntity.ok(previewService.getPreviewInfo(documentId));
    }
}

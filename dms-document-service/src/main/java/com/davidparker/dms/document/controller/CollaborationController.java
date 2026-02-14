package com.davidparker.dms.document.controller;

import com.davidparker.dms.document.model.DocumentComment;
import com.davidparker.dms.document.model.DocumentFavorite;
import com.davidparker.dms.document.model.DocumentShare;
import com.davidparker.dms.document.service.CollaborationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
public class CollaborationController {

    private final CollaborationService collaborationService;

    public CollaborationController(CollaborationService collaborationService) {
        this.collaborationService = collaborationService;
    }

    // --- Comments ---
    @GetMapping("/{documentId}/comments")
    public ResponseEntity<List<DocumentComment>> getComments(@PathVariable UUID documentId) {
        return ResponseEntity.ok(collaborationService.getComments(documentId));
    }

    @PostMapping("/{documentId}/comments")
    public ResponseEntity<DocumentComment> addComment(
            @PathVariable UUID documentId,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(collaborationService.addComment(
            documentId, body.get("content"), body.get("parentId") != null ? UUID.fromString(body.get("parentId")) : null));
    }

    @DeleteMapping("/{documentId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID documentId, @PathVariable UUID commentId) {
        collaborationService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    // --- Sharing ---
    @GetMapping("/{documentId}/shares")
    public ResponseEntity<List<DocumentShare>> getShares(@PathVariable UUID documentId) {
        return ResponseEntity.ok(collaborationService.getShares(documentId));
    }

    @PostMapping("/{documentId}/shares")
    public ResponseEntity<DocumentShare> shareDocument(
            @PathVariable UUID documentId,
            @RequestBody Map<String, String> body) {
        DocumentShare.SharePermission perm = DocumentShare.SharePermission.valueOf(
            body.getOrDefault("permission", "VIEW"));
        return ResponseEntity.ok(collaborationService.shareDocument(
            documentId, UUID.fromString(body.get("userId")), perm));
    }

    @DeleteMapping("/{documentId}/shares/{shareId}")
    public ResponseEntity<Void> revokeShare(@PathVariable UUID documentId, @PathVariable UUID shareId) {
        collaborationService.revokeShare(shareId);
        return ResponseEntity.noContent().build();
    }

    // --- Favorites ---
    @GetMapping("/favorites")
    public ResponseEntity<Page<DocumentFavorite>> getFavorites(
            @RequestParam UUID userId, Pageable pageable) {
        return ResponseEntity.ok(collaborationService.getFavorites(userId, pageable));
    }

    @PostMapping("/{documentId}/favorites")
    public ResponseEntity<DocumentFavorite> addFavorite(
            @PathVariable UUID documentId, @RequestParam UUID userId) {
        return ResponseEntity.ok(collaborationService.addFavorite(userId, documentId));
    }

    @DeleteMapping("/{documentId}/favorites")
    public ResponseEntity<Void> removeFavorite(
            @PathVariable UUID documentId, @RequestParam UUID userId) {
        collaborationService.removeFavorite(userId, documentId);
        return ResponseEntity.noContent().build();
    }

    // --- Recent Documents ---
    @GetMapping("/recent")
    public ResponseEntity<List<UUID>> getRecentDocuments(@RequestParam UUID userId) {
        return ResponseEntity.ok(collaborationService.getRecentDocuments(userId));
    }
}

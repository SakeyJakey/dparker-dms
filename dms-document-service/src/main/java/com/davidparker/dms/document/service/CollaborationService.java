package com.davidparker.dms.document.service;

import com.davidparker.dms.document.model.DocumentComment;
import com.davidparker.dms.document.model.DocumentFavorite;
import com.davidparker.dms.document.model.DocumentShare;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class CollaborationService {

    private final Map<UUID, List<DocumentComment>> commentsStore = new ConcurrentHashMap<>();
    private final Map<UUID, List<DocumentShare>> sharesStore = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> favoritesStore = new ConcurrentHashMap<>();
    private final Map<UUID, LinkedList<UUID>> recentStore = new ConcurrentHashMap<>();
    private final AuditEventClient auditEventClient;

    public CollaborationService(AuditEventClient auditEventClient) {
        this.auditEventClient = auditEventClient;
    }

    // --- Comments ---
    public List<DocumentComment> getComments(UUID documentId) {
        return commentsStore.getOrDefault(documentId, new ArrayList<>());
    }

    public DocumentComment addComment(UUID documentId, String content, UUID parentId) {
        DocumentComment comment = DocumentComment.builder()
            .id(UUID.randomUUID())
            .documentId(documentId)
            .userId(UUID.randomUUID())
            .content(content)
            .parentId(parentId)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
        commentsStore.computeIfAbsent(documentId, k -> new ArrayList<>()).add(comment);
        return comment;
    }

    public void deleteComment(UUID commentId) {
        commentsStore.values().forEach(list -> list.removeIf(c -> c.getId().equals(commentId)));
    }

    // --- Sharing ---
    public List<DocumentShare> getShares(UUID documentId) {
        return sharesStore.getOrDefault(documentId, new ArrayList<>());
    }

    public DocumentShare shareDocument(UUID documentId, UUID userId, DocumentShare.SharePermission permission) {
        DocumentShare share = DocumentShare.builder()
            .id(UUID.randomUUID())
            .documentId(documentId)
            .sharedWithUserId(userId)
            .sharedByUserId(UUID.randomUUID())
            .permission(permission)
            .createdAt(Instant.now())
            .build();
        sharesStore.computeIfAbsent(documentId, k -> new ArrayList<>()).add(share);

        auditEventClient.logEvent(Map.of(
            "eventType", "SHARE", "eventCategory", "DOCUMENT_ACCESS",
            "action", "DOCUMENT_SHARED", "result", "SUCCESS",
            "resourceType", "Document", "resourceId", documentId.toString(),
            "timestamp", Instant.now().toString()
        ));
        return share;
    }

    public void revokeShare(UUID shareId) {
        sharesStore.values().forEach(list -> list.removeIf(s -> s.getId().equals(shareId)));
    }

    // --- Favorites ---
    public Page<DocumentFavorite> getFavorites(UUID userId, Pageable pageable) {
        Set<UUID> docIds = favoritesStore.getOrDefault(userId, new HashSet<>());
        List<DocumentFavorite> favorites = docIds.stream()
            .map(docId -> DocumentFavorite.builder()
                .id(UUID.randomUUID()).userId(userId).documentId(docId).createdAt(Instant.now()).build())
            .collect(Collectors.toList());
        return new PageImpl<>(favorites, pageable, favorites.size());
    }

    public DocumentFavorite addFavorite(UUID userId, UUID documentId) {
        favoritesStore.computeIfAbsent(userId, k -> new HashSet<>()).add(documentId);
        return DocumentFavorite.builder()
            .id(UUID.randomUUID()).userId(userId).documentId(documentId).createdAt(Instant.now()).build();
    }

    public void removeFavorite(UUID userId, UUID documentId) {
        favoritesStore.getOrDefault(userId, new HashSet<>()).remove(documentId);
    }

    // --- Recent ---
    public List<UUID> getRecentDocuments(UUID userId) {
        return new ArrayList<>(recentStore.getOrDefault(userId, new LinkedList<>()));
    }

    public void trackRecentDocument(UUID userId, UUID documentId) {
        LinkedList<UUID> recent = recentStore.computeIfAbsent(userId, k -> new LinkedList<>());
        recent.remove(documentId);
        recent.addFirst(documentId);
        while (recent.size() > 20) recent.removeLast();
    }
}

package com.davidparker.dms.document.service;

import com.davidparker.dms.document.model.DocumentComment;
import com.davidparker.dms.document.model.DocumentFavorite;
import com.davidparker.dms.document.model.DocumentShare;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class CollaborationServiceTest {

    @Mock
    private AuditEventClient auditEventClient;

    private CollaborationService collaborationService;
    private UUID documentId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        collaborationService = new CollaborationService(auditEventClient);
        documentId = UUID.randomUUID();
        userId = UUID.randomUUID();
        lenient().doNothing().when(auditEventClient).logEvent(any());
    }

    @Test
    void testAddAndGetComments() {
        DocumentComment comment = collaborationService.addComment(documentId, "Great document!", null);

        assertNotNull(comment.getId());
        assertEquals("Great document!", comment.getContent());
        assertEquals(documentId, comment.getDocumentId());

        List<DocumentComment> comments = collaborationService.getComments(documentId);
        assertEquals(1, comments.size());
    }

    @Test
    void testDeleteComment() {
        DocumentComment comment = collaborationService.addComment(documentId, "To delete", null);
        collaborationService.deleteComment(comment.getId());

        List<DocumentComment> comments = collaborationService.getComments(documentId);
        assertEquals(0, comments.size());
    }

    @Test
    void testShareDocument() {
        DocumentShare share = collaborationService.shareDocument(
            documentId, userId, DocumentShare.SharePermission.VIEW);

        assertNotNull(share.getId());
        assertEquals(documentId, share.getDocumentId());
        assertEquals(DocumentShare.SharePermission.VIEW, share.getPermission());

        List<DocumentShare> shares = collaborationService.getShares(documentId);
        assertEquals(1, shares.size());
    }

    @Test
    void testRevokeShare() {
        DocumentShare share = collaborationService.shareDocument(
            documentId, userId, DocumentShare.SharePermission.EDIT);
        collaborationService.revokeShare(share.getId());

        List<DocumentShare> shares = collaborationService.getShares(documentId);
        assertEquals(0, shares.size());
    }

    @Test
    void testAddAndRemoveFavorite() {
        DocumentFavorite fav = collaborationService.addFavorite(userId, documentId);
        assertNotNull(fav);
        assertEquals(userId, fav.getUserId());
        assertEquals(documentId, fav.getDocumentId());

        Page<DocumentFavorite> favorites = collaborationService.getFavorites(userId, PageRequest.of(0, 10));
        assertEquals(1, favorites.getTotalElements());

        collaborationService.removeFavorite(userId, documentId);
        favorites = collaborationService.getFavorites(userId, PageRequest.of(0, 10));
        assertEquals(0, favorites.getTotalElements());
    }

    @Test
    void testRecentDocuments() {
        UUID doc1 = UUID.randomUUID();
        UUID doc2 = UUID.randomUUID();

        collaborationService.trackRecentDocument(userId, doc1);
        collaborationService.trackRecentDocument(userId, doc2);

        List<UUID> recent = collaborationService.getRecentDocuments(userId);
        assertEquals(2, recent.size());
        assertEquals(doc2, recent.get(0)); // most recent first
    }

    @Test
    void testGetCommentsEmptyDocument() {
        List<DocumentComment> comments = collaborationService.getComments(UUID.randomUUID());
        assertTrue(comments.isEmpty());
    }
}

package com.davidparker.dms.document.service;

import com.davidparker.dms.document.dto.SearchRequest;
import com.davidparker.dms.document.model.Document;
import com.davidparker.dms.document.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    private SearchService searchService;

    @BeforeEach
    void setUp() {
        searchService = new SearchService(documentRepository);
    }

    @Test
    void testSearchByApplicationId() {
        UUID appId = UUID.randomUUID();
        SearchRequest request = new SearchRequest();
        request.setApplicationId(appId);
        request.setPage(0);
        request.setSize(20);

        Page<Document> expected = new PageImpl<>(List.of());
        when(documentRepository.findByApplicationId(eq(appId), any())).thenReturn(expected);

        Page<Document> result = searchService.search(request);

        assertNotNull(result);
        verify(documentRepository).findByApplicationId(eq(appId), any());
    }

    @Test
    void testSearchWithClassification() {
        UUID appId = UUID.randomUUID();
        SearchRequest request = new SearchRequest();
        request.setApplicationId(appId);
        request.setClassifications(List.of(Document.Classification.CONFIDENTIAL));
        request.setPage(0);
        request.setSize(20);

        Page<Document> expected = new PageImpl<>(List.of());
        when(documentRepository.findByApplicationIdAndClassification(eq(appId), eq(Document.Classification.CONFIDENTIAL), any())).thenReturn(expected);

        Page<Document> result = searchService.search(request);

        assertNotNull(result);
        verify(documentRepository).findByApplicationIdAndClassification(eq(appId), eq(Document.Classification.CONFIDENTIAL), any());
    }

    @Test
    void testSearchAll() {
        SearchRequest request = new SearchRequest();
        request.setPage(0);
        request.setSize(20);

        Page<Document> expected = new PageImpl<>(List.of());
        when(documentRepository.findAll(any(PageRequest.class))).thenReturn(expected);

        Page<Document> result = searchService.search(request);

        assertNotNull(result);
        verify(documentRepository).findAll(any(PageRequest.class));
    }

    @Test
    void testFulltextSearch() {
        Page<Document> expected = new PageImpl<>(List.of());
        when(documentRepository.searchByName(eq("test"), any())).thenReturn(expected);

        Page<Document> result = searchService.fulltextSearch("test", 0, 20);

        assertNotNull(result);
        verify(documentRepository).searchByName(eq("test"), any());
    }
}

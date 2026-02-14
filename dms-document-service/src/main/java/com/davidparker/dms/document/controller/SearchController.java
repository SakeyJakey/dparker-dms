package com.davidparker.dms.document.controller;

import com.davidparker.dms.document.dto.SearchRequest;
import com.davidparker.dms.document.model.Document;
import com.davidparker.dms.document.service.SearchService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/documents/search")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Document Search", description = "Full-text and advanced document search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping
    public ResponseEntity<Page<Document>> search(@RequestBody SearchRequest request) {
        return ResponseEntity.ok(searchService.search(request));
    }

    @GetMapping("/fulltext")
    public ResponseEntity<Page<Document>> fulltextSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(searchService.fulltextSearch(query, page, size));
    }
}

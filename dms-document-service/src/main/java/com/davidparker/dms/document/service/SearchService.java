package com.davidparker.dms.document.service;

import com.davidparker.dms.document.dto.SearchRequest;
import com.davidparker.dms.document.model.Document;
import com.davidparker.dms.document.repository.DocumentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    private final DocumentRepository documentRepository;

    public SearchService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public Page<Document> search(SearchRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize());

        if (request.getApplicationId() != null && request.getClassifications() != null && !request.getClassifications().isEmpty()) {
            return documentRepository.findByApplicationIdAndClassification(
                request.getApplicationId(), request.getClassifications().get(0), pageRequest);
        }

        if (request.getApplicationId() != null) {
            return documentRepository.findByApplicationId(request.getApplicationId(), pageRequest);
        }

        return documentRepository.findAll(pageRequest);
    }

    public Page<Document> fulltextSearch(String query, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return documentRepository.searchByName(query, pageRequest);
    }
}

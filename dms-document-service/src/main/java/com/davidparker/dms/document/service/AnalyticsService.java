package com.davidparker.dms.document.service;

import com.davidparker.dms.document.dto.DashboardAnalytics;
import com.davidparker.dms.document.model.Document;
import com.davidparker.dms.document.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AnalyticsService {

    private final DocumentRepository documentRepository;

    public AnalyticsService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public DashboardAnalytics getDashboardAnalytics(UUID applicationId) {
        long totalDocuments = applicationId != null
            ? documentRepository.countByApplicationId(applicationId)
            : documentRepository.count();

        Map<String, Long> byClassification = new LinkedHashMap<>();
        Arrays.stream(Document.Classification.values()).forEach(c ->
            byClassification.put(c.name(), documentRepository.countByClassification(c)));

        long pciDocuments = documentRepository.countByPciRelevantTrue();

        return DashboardAnalytics.builder()
            .totalDocuments(totalDocuments)
            .documentsThisMonth(totalDocuments)
            .documentsThisWeek(0)
            .documentsByClassification(byClassification)
            .documentsByMonth(Map.of())
            .pciDocuments(pciDocuments)
            .gdprDocuments(0)
            .storageUsedMb(0.0)
            .activeUsers(0)
            .totalQueries(0)
            .build();
    }
}

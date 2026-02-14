package com.davidparker.dms.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class DashboardAnalytics {
    private long totalDocuments;
    private long documentsThisMonth;
    private long documentsThisWeek;
    private Map<String, Long> documentsByClassification;
    private Map<String, Long> documentsByMonth;
    private long pciDocuments;
    private long gdprDocuments;
    private double storageUsedMb;
    private long activeUsers;
    private long totalQueries;
}

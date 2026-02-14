package com.davidparker.dms.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class BulkOperationResponse {
    private int totalRequested;
    private int successCount;
    private int failureCount;
    private List<UUID> failedIds;
    private String message;
}

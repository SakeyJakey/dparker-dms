package com.davidparker.dms.dms_document_service.dto;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class DocumentVersionResponse {
    private Integer version;
    private UUID documentId;
    private Instant createdAt;
    private UUID createdBy;
    private String blobUrl;
}

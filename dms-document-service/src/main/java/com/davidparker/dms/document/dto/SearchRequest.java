package com.davidparker.dms.document.dto;

import com.davidparker.dms.document.model.Document;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class SearchRequest {
    private String query;
    private UUID applicationId;
    private List<Document.Classification> classifications;
    private Instant dateFrom;
    private Instant dateTo;
    private List<String> tags;
    private int page = 0;
    private int size = 20;
}

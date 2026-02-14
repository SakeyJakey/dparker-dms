package com.davidparker.dms.document.dto;

import com.davidparker.dms.document.model.Document;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BulkOperationRequest {
    @NotEmpty
    private List<UUID> documentIds;

    @NotNull
    private BulkAction action;

    private Document.Classification targetClassification;
    private List<String> tags;

    public enum BulkAction {
        DELETE, CLASSIFY, TAG, ARCHIVE
    }
}

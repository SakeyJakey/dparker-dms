package com.davidparker.dms.document.controller;

import com.davidparker.dms.document.dto.BulkOperationRequest;
import com.davidparker.dms.document.dto.BulkOperationResponse;
import com.davidparker.dms.document.service.BulkOperationsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documents/bulk")
public class BulkOperationsController {

    private final BulkOperationsService bulkOperationsService;

    public BulkOperationsController(BulkOperationsService bulkOperationsService) {
        this.bulkOperationsService = bulkOperationsService;
    }

    @PostMapping("/operation")
    public ResponseEntity<BulkOperationResponse> executeBulkOperation(
            @Valid @RequestBody BulkOperationRequest request) {
        return ResponseEntity.ok(bulkOperationsService.executeBulkOperation(request));
    }

    @PostMapping("/upload")
    public ResponseEntity<BulkOperationResponse> bulkUpload(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("classification") String classification,
            @RequestParam("applicationId") String applicationId) {
        return ResponseEntity.ok(bulkOperationsService.bulkUpload(files, classification, applicationId));
    }
}

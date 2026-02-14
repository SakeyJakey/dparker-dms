package com.davidparker.dms.admin.controller;

import com.davidparker.dms.admin.model.ApiKey;
import com.davidparker.dms.admin.service.ApiKeyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/api-keys")
@PreAuthorize("hasRole('DMS.Admin')")
@Tag(name = "API Key Management", description = "Manage API keys for programmatic access")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @GetMapping
    public ResponseEntity<Page<ApiKey>> listApiKeys(Pageable pageable) {
        return ResponseEntity.ok(apiKeyService.listApiKeys(pageable));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createApiKey(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(apiKeyService.createApiKey(
            request.get("name"),
            request.get("scopes"),
            request.get("applicationId") != null ? UUID.fromString(request.get("applicationId")) : null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revokeApiKey(@PathVariable UUID id) {
        apiKeyService.revokeApiKey(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/regenerate")
    public ResponseEntity<Map<String, Object>> regenerateApiKey(@PathVariable UUID id) {
        return ResponseEntity.ok(apiKeyService.regenerateApiKey(id));
    }
}

package com.davidparker.dms.admin.service;

import com.davidparker.dms.admin.model.ApiKey;
import com.davidparker.dms.admin.repository.ApiKeyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.davidparker.dms.admin.exception.ResourceNotFoundException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final AuditEventClient auditEventClient;
    private final SecureRandom secureRandom = new SecureRandom();

    public ApiKeyService(ApiKeyRepository apiKeyRepository, AuditEventClient auditEventClient) {
        this.apiKeyRepository = apiKeyRepository;
        this.auditEventClient = auditEventClient;
    }

    public Page<ApiKey> listApiKeys(Pageable pageable) {
        return apiKeyRepository.findAll(pageable);
    }

    public Map<String, Object> createApiKey(String name, String scopes, UUID applicationId) {
        String rawKey = generateKey();
        String keyHash = hashKey(rawKey);
        String keyPrefix = rawKey.substring(0, 8);

        ApiKey apiKey = ApiKey.builder()
            .name(name)
            .keyHash(keyHash)
            .keyPrefix(keyPrefix)
            .applicationId(applicationId)
            .scopes(scopes != null ? scopes : "read")
            .active(true)
            .build();

        apiKey = apiKeyRepository.save(apiKey);

        auditEventClient.logEvent(Map.of(
            "eventType", "CREATE", "eventCategory", "ADMIN_ACTIONS",
            "action", "API_KEY_CREATED", "result", "SUCCESS",
            "resourceType", "ApiKey", "resourceId", apiKey.getId().toString(),
            "timestamp", Instant.now().toString()
        ));

        return Map.of(
            "id", apiKey.getId().toString(),
            "name", apiKey.getName(),
            "key", rawKey,
            "prefix", keyPrefix,
            "scopes", apiKey.getScopes(),
            "message", "Store this key securely - it will not be shown again"
        );
    }

    public void revokeApiKey(UUID id) {
        ApiKey apiKey = apiKeyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ApiKey", id.toString()));
        apiKey.setActive(false);
        apiKeyRepository.save(apiKey);

        auditEventClient.logEvent(Map.of(
            "eventType", "REVOKE", "eventCategory", "ADMIN_ACTIONS",
            "action", "API_KEY_REVOKED", "result", "SUCCESS",
            "resourceType", "ApiKey", "resourceId", id.toString(),
            "timestamp", Instant.now().toString()
        ));
    }

    public Map<String, Object> regenerateApiKey(UUID id) {
        ApiKey existing = apiKeyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ApiKey", id.toString()));
        existing.setActive(false);
        apiKeyRepository.save(existing);
        return createApiKey(existing.getName(), existing.getScopes(), existing.getApplicationId());
    }

    private String generateKey() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return "dms_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashKey(String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash key", e);
        }
    }
}

package com.davidparker.dms.service;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class SecureCredentialService {

    private final SecretClient secretClient;
    private final Cache<String, String> secretCache;
    private final AuditService auditService;

    public SecureCredentialService(SecretClient secretClient, AuditService auditService) {
        this.secretClient = secretClient;
        this.auditService = auditService;
        this.secretCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5))
            .maximumSize(100)
            .build(this::fetchSecret);
    }

    public String getSecret(String secretName) {
        return secretCache.get(secretName);
    }

    private String fetchSecret(String secretName) {
        KeyVaultSecret secret = secretClient.getSecret(secretName);
        // Audit secret access
        // auditService.logSecretAccess(secretName);
        return secret.getValue();
    }
}

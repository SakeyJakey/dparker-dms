package com.davidparker.dms.core.util;

import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Shared service for securely retrieving secrets and keys from Azure Key Vault.
 */
@Service
public class SecureCredentialService {

    private final KeyClient keyClient;

    @Autowired(required = false)
    public SecureCredentialService(KeyClient keyClient) {
        this.keyClient = keyClient;
    }

    /**
     * Retrieves a key from Azure Key Vault.
     * 
     * @param keyName The name of the key to retrieve
     * @return Optional containing the key if found
     */
    public Optional<KeyVaultKey> getKey(String keyName) {
        if (keyClient == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(keyClient.getKey(keyName));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Checks if a key exists in Azure Key Vault.
     * 
     * @param keyName The name of the key to check
     * @return true if the key exists, false otherwise
     */
    public boolean keyExists(String keyName) {
        return getKey(keyName).isPresent();
    }
}

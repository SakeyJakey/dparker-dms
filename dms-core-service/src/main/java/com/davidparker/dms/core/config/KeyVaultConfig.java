package com.davidparker.dms.core.config;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Shared Azure Key Vault configuration for all DMS services.
 */
@Configuration
public class KeyVaultConfig {

    @Value("${spring.cloud.azure.keyvault.secret.property-sources[0].endpoint:}")
    private String keyVaultEndpoint;

    @Bean
    public TokenCredential tokenCredential() {
        return new DefaultAzureCredentialBuilder().build();
    }

    @Bean
    public KeyClient keyClient(TokenCredential tokenCredential) {
        if (keyVaultEndpoint == null || keyVaultEndpoint.isEmpty()) {
            return null;
        }
        return new KeyClientBuilder()
            .vaultUrl(keyVaultEndpoint)
            .credential(tokenCredential)
            .buildClient();
    }
}

package com.davidparker.dms.config;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeyVaultConfig {

    @Value("${spring.cloud.azure.keyvault.secret.property-sources[0].endpoint}")
    private String vaultUrl;

    @Bean
    public TokenCredential tokenCredential() {
        return new DefaultAzureCredentialBuilder().build();
    }

    @Bean
    public KeyClient keyClient(TokenCredential tokenCredential) {
        return new KeyClientBuilder()
            .vaultUrl(vaultUrl)
            .credential(tokenCredential)
            .buildClient();
    }

    @Bean
    public SecretClient secretClient(TokenCredential tokenCredential) {
        return new SecretClientBuilder()
            .vaultUrl(vaultUrl)
            .credential(tokenCredential)
            .buildClient();
    }
}

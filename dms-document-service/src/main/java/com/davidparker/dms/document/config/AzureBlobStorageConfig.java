package com.davidparker.dms.document.config;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Provides {@link BlobServiceClient} for document upload/download.
 * Prefers explicit connection string or account credentials; falls back to
 * Azurite development storage when {@code dms.storage.blob.use-development-storage=true}.
 */
@Configuration
public class AzureBlobStorageConfig {

    private static final String DEVELOPMENT_STORAGE_CONNECTION = "UseDevelopmentStorage=true";

    @Bean
    @ConditionalOnMissingBean
    public BlobServiceClient blobServiceClient(
            @Value("${spring.cloud.azure.storage.blob.connection-string:}") String connectionString,
            @Value("${spring.cloud.azure.storage.blob.account-name:}") String accountName,
            @Value("${spring.cloud.azure.storage.blob.account-key:}") String accountKey,
            @Value("${dms.storage.blob.use-development-storage:false}") boolean useDevelopmentStorage) {

        if (StringUtils.hasText(connectionString)) {
            return new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
        }

        if (StringUtils.hasText(accountName) && StringUtils.hasText(accountKey)) {
            String builtConnectionString = String.format(
                    "DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net",
                    accountName,
                    accountKey);
            return new BlobServiceClientBuilder()
                    .connectionString(builtConnectionString)
                    .buildClient();
        }

        if (useDevelopmentStorage) {
            return new BlobServiceClientBuilder()
                    .connectionString(DEVELOPMENT_STORAGE_CONNECTION)
                    .buildClient();
        }

        throw new IllegalStateException(
                "Azure Blob Storage is not configured. Set spring.cloud.azure.storage.blob.connection-string, "
                        + "or account-name and account-key, or dms.storage.blob.use-development-storage=true "
                        + "(requires Azurite or Azure Storage Emulator).");
    }
}

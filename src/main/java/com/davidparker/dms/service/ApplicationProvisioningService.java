package com.davidparker.dms.service;

import com.azure.core.credential.TokenCredential;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.davidparker.dms.model.RegisteredApplication;
import com.davidparker.dms.repository.RegisteredApplicationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ApplicationProvisioningService {

    private final BlobServiceClient blobServiceClient;
    private final KeyClient keyClient;
    private final RegisteredApplicationRepository repository;

    public ApplicationProvisioningService(
            BlobServiceClient blobServiceClient,
            KeyClient keyClient,
            RegisteredApplicationRepository repository) {
        this.blobServiceClient = blobServiceClient;
        this.keyClient = keyClient;
        this.repository = repository;
    }

    @Transactional
    public RegisteredApplication provisionApplication(ApplicationProvisionRequest request) {
        // 1. Create dedicated storage container
        String containerName = request.getApplicationName().toLowerCase().replaceAll("[^a-z0-9-]", "-") + "-documents";
        BlobContainerClient container = blobServiceClient.createBlobContainerIfNotExists(containerName);

        // 2. Create application-specific encryption key in Key Vault
        String keyName = request.getApplicationName().toLowerCase().replaceAll("[^a-z0-9-]", "-") + "-encryption-key";
        try {
            keyClient.createRsaKey(new CreateRsaKeyOptions(keyName).setKeySize(2048));
        } catch (Exception e) {
            // Key might already exist, continue
        }

        // 3. Configure container encryption with CMK (would need additional Azure SDK calls)
        // configureContainerEncryption(containerName, keyName);

        // 4. Set up RBAC for the application's service principal (would need Azure SDK calls)
        // assignStorageRbac(request.getEntraAppId(), containerName);

        // 5. Register application in database
        RegisteredApplication app = RegisteredApplication.builder()
            .id(UUID.randomUUID())
            .entraAppId(request.getEntraAppId())
            .applicationName(request.getApplicationName())
            .storageContainerName(containerName)
            .encryptionKeyName(keyName)
            .status(RegisteredApplication.ApplicationStatus.ACTIVE)
            .build();

        return repository.save(app);
    }

    public static class ApplicationProvisionRequest {
        private String entraAppId;
        private String applicationName;

        public String getEntraAppId() {
            return entraAppId;
        }

        public void setEntraAppId(String entraAppId) {
            this.entraAppId = entraAppId;
        }

        public String getApplicationName() {
            return applicationName;
        }

        public void setApplicationName(String applicationName) {
            this.applicationName = applicationName;
        }
    }
}

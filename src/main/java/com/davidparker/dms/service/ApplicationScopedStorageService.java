package com.davidparker.dms.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.davidparker.dms.model.RegisteredApplication;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.UUID;

@Service
public class ApplicationScopedStorageService {

    private final BlobServiceClient blobServiceClient;

    public ApplicationScopedStorageService(BlobServiceClient blobServiceClient) {
        this.blobServiceClient = blobServiceClient;
    }

    public BlobContainerClient getContainerForCurrentApplication() {
        RegisteredApplication app = ApplicationContext.getCurrent();
        if (app == null) {
            throw new IllegalStateException("No application context set for current request");
        }
        return blobServiceClient.getBlobContainerClient(app.getStorageContainerName());
    }

    public String uploadDocument(UUID documentId, InputStream content) {
        BlobContainerClient container = getContainerForCurrentApplication();
        String blobPath = buildBlobPath(documentId);
        
        BlobClient blobClient = container.getBlobClient(blobPath);
        blobClient.upload(content, true);
        
        return blobClient.getBlobUrl();
    }

    private String buildBlobPath(UUID documentId) {
        // Path structure: {year}/{month}/{document-id}/v1/content
        java.time.LocalDate now = java.time.LocalDate.now();
        return String.format("%d/%02d/%s/v1/content", 
            now.getYear(), 
            now.getMonthValue(), 
            documentId.toString());
    }
}

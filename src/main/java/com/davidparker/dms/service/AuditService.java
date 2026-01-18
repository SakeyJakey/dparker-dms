package com.davidparker.dms.service;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.EventHubProducerClientBuilder;
import com.davidparker.dms.model.AuditEvent;
import com.davidparker.dms.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private final EventHubProducerClient eventHubProducerClient;
    private final MessageDigest sha256Digest;

    public AuditService(
            AuditLogRepository auditLogRepository,
            ObjectMapper objectMapper,
            @Value("${azure.event-hubs.connection-string:}") String eventHubsConnectionString) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
        
        if (eventHubsConnectionString != null && !eventHubsConnectionString.isEmpty()) {
            this.eventHubProducerClient = new EventHubProducerClientBuilder()
                .connectionString(eventHubsConnectionString, "audit-logs")
                .buildClient();
        } else {
            this.eventHubProducerClient = null;
        }
        
        try {
            this.sha256Digest = MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize SHA-256 digest", e);
        }
    }

    @Async
    @Transactional
    public void logEvent(AuditEvent event) {
        // Set timestamp if not set
        if (event.getTimestamp() == null) {
            event.setTimestamp(Instant.now());
        }

        // Generate event ID if not set
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }

        // Calculate checksum
        String checksum = calculateChecksum(event);
        event.setChecksum(checksum);

        // Save to database
        auditLogRepository.save(event);

        // Publish to Event Hubs if configured
        if (eventHubProducerClient != null) {
            publishToEventHub(event);
        }
    }

    private String calculateChecksum(AuditEvent event) {
        try {
            String eventData = objectMapper.writeValueAsString(event);
            byte[] hash = sha256Digest.digest(eventData.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate checksum", e);
        }
    }

    private void publishToEventHub(AuditEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            EventData eventData = new EventData(eventJson.getBytes(StandardCharsets.UTF_8));
            eventHubProducerClient.createBatch().addEvent(eventData);
        } catch (Exception e) {
            // Log error but don't fail the audit logging
            System.err.println("Failed to publish audit event to Event Hub: " + e.getMessage());
        }
    }
}

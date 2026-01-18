package com.davidparker.dms.audit.service;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.davidparker.dms.audit.model.AuditEvent;
import com.davidparker.dms.audit.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Map;
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
            @Value("${azure.event-hubs.connection-string:}") String eventHubsConnectionString,
            @Value("${azure.event-hubs.hub-name:audit-logs}") String hubName) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
        
        if (eventHubsConnectionString != null && !eventHubsConnectionString.isEmpty()) {
            this.eventHubProducerClient = new EventHubClientBuilder()
                .connectionString(eventHubsConnectionString, hubName)
                .buildProducerClient();
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
    public void logEvent(Map<String, Object> eventData) {
        AuditEvent event = mapToAuditEvent(eventData);
        
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

    private AuditEvent mapToAuditEvent(Map<String, Object> eventData) {
        AuditEvent.AuditEventBuilder builder = AuditEvent.builder();
        
        if (eventData.get("eventType") != null) {
            builder.eventType(AuditEvent.EventType.valueOf(eventData.get("eventType").toString()));
        }
        if (eventData.get("eventCategory") != null) {
            builder.eventCategory(AuditEvent.EventCategory.valueOf(eventData.get("eventCategory").toString()));
        }
        if (eventData.get("action") != null) {
            builder.action(eventData.get("action").toString());
        }
        if (eventData.get("result") != null) {
            builder.result(AuditEvent.AuditResult.valueOf(eventData.get("result").toString()));
        }
        if (eventData.get("resourceId") != null) {
            builder.resourceId(UUID.fromString(eventData.get("resourceId").toString()));
        }
        if (eventData.get("applicationId") != null) {
            builder.applicationId(UUID.fromString(eventData.get("applicationId").toString()));
        }
        if (eventData.get("timestamp") != null) {
            builder.timestamp(Instant.parse(eventData.get("timestamp").toString()));
        }
        
        return builder.build();
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
            
            // Create a batch and add the event
            com.azure.messaging.eventhubs.EventDataBatch batch = eventHubProducerClient.createBatch();
            if (batch.tryAdd(eventData)) {
                eventHubProducerClient.send(batch);
            } else {
                // Event is too large for batch, send directly
                eventHubProducerClient.send(java.util.Collections.singletonList(eventData));
            }
        } catch (Exception e) {
            System.err.println("Failed to publish audit event to Event Hub: " + e.getMessage());
        }
    }
}

package com.davidparker.dms.admin.service;

import com.davidparker.dms.admin.model.Webhook;
import com.davidparker.dms.admin.repository.WebhookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class WebhookService {

    private final WebhookRepository webhookRepository;
    private final AuditEventClient auditEventClient;

    public WebhookService(WebhookRepository webhookRepository, AuditEventClient auditEventClient) {
        this.webhookRepository = webhookRepository;
        this.auditEventClient = auditEventClient;
    }

    public Page<Webhook> listWebhooks(Pageable pageable) {
        return webhookRepository.findAll(pageable);
    }

    public Webhook createWebhook(Webhook webhook) {
        webhook = webhookRepository.save(webhook);
        auditEventClient.logEvent(Map.of(
            "eventType", "CREATE", "eventCategory", "ADMIN_ACTIONS",
            "action", "WEBHOOK_CREATED", "result", "SUCCESS",
            "resourceType", "Webhook", "resourceId", webhook.getId().toString(),
            "timestamp", Instant.now().toString()
        ));
        return webhook;
    }

    public Webhook updateWebhook(UUID id, Webhook update) {
        Webhook existing = webhookRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Webhook not found"));
        existing.setName(update.getName());
        existing.setUrl(update.getUrl());
        existing.setEventTypes(update.getEventTypes());
        existing.setActive(update.getActive());
        return webhookRepository.save(existing);
    }

    public void deleteWebhook(UUID id) {
        webhookRepository.deleteById(id);
    }

    public String testWebhook(UUID id) {
        Webhook webhook = webhookRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Webhook not found"));
        try {
            WebClient.create(webhook.getUrl())
                .post()
                .bodyValue(Map.of("event", "test", "timestamp", Instant.now().toString()))
                .retrieve()
                .bodyToMono(String.class)
                .block();
            return "Webhook test successful";
        } catch (Exception e) {
            return "Webhook test failed: " + e.getMessage();
        }
    }
}

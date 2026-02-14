package com.davidparker.dms.admin.controller;

import com.davidparker.dms.admin.model.Webhook;
import com.davidparker.dms.admin.service.WebhookService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/webhooks")
@PreAuthorize("hasRole('DMS.Admin')")
@Tag(name = "Webhook Management", description = "Manage webhook subscriptions for document events")
public class WebhookController {

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @GetMapping
    public ResponseEntity<Page<Webhook>> listWebhooks(Pageable pageable) {
        return ResponseEntity.ok(webhookService.listWebhooks(pageable));
    }

    @PostMapping
    public ResponseEntity<Webhook> createWebhook(@RequestBody Webhook webhook) {
        return ResponseEntity.ok(webhookService.createWebhook(webhook));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Webhook> updateWebhook(@PathVariable UUID id, @RequestBody Webhook webhook) {
        return ResponseEntity.ok(webhookService.updateWebhook(id, webhook));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWebhook(@PathVariable UUID id) {
        webhookService.deleteWebhook(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/test")
    public ResponseEntity<String> testWebhook(@PathVariable UUID id) {
        return ResponseEntity.ok(webhookService.testWebhook(id));
    }
}

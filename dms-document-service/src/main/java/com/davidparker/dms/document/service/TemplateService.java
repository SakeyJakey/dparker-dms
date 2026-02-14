package com.davidparker.dms.document.service;

import com.davidparker.dms.document.model.DocumentTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TemplateService {

    private final Map<UUID, DocumentTemplate> templateStore = new ConcurrentHashMap<>();

    public Page<DocumentTemplate> listTemplates(Pageable pageable) {
        List<DocumentTemplate> templates = new ArrayList<>(templateStore.values());
        return new PageImpl<>(templates, pageable, templates.size());
    }

    public DocumentTemplate getTemplate(UUID id) {
        DocumentTemplate template = templateStore.get(id);
        if (template == null) throw new RuntimeException("Template not found");
        return template;
    }

    public DocumentTemplate createTemplate(DocumentTemplate template) {
        template.setId(UUID.randomUUID());
        template.setCreatedAt(Instant.now());
        template.setUpdatedAt(Instant.now());
        templateStore.put(template.getId(), template);
        return template;
    }

    public DocumentTemplate updateTemplate(UUID id, DocumentTemplate update) {
        DocumentTemplate existing = getTemplate(id);
        existing.setName(update.getName());
        existing.setDescription(update.getDescription());
        existing.setDefaultClassification(update.getDefaultClassification());
        existing.setContentTemplate(update.getContentTemplate());
        existing.setMetadataSchema(update.getMetadataSchema());
        existing.setUpdatedAt(Instant.now());
        templateStore.put(id, existing);
        return existing;
    }

    public void deleteTemplate(UUID id) {
        templateStore.remove(id);
    }
}

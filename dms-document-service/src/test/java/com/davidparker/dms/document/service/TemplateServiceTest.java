package com.davidparker.dms.document.service;

import com.davidparker.dms.document.model.Document;
import com.davidparker.dms.document.model.DocumentTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TemplateServiceTest {

    private TemplateService templateService;

    @BeforeEach
    void setUp() {
        templateService = new TemplateService();
    }

    @Test
    void testCreateAndGetTemplate() {
        DocumentTemplate template = DocumentTemplate.builder()
            .name("Invoice Template")
            .description("Standard invoice")
            .defaultClassification(Document.Classification.INTERNAL)
            .contentTemplate("Invoice content")
            .build();

        DocumentTemplate created = templateService.createTemplate(template);

        assertNotNull(created.getId());
        assertNotNull(created.getCreatedAt());
        assertEquals("Invoice Template", created.getName());

        DocumentTemplate retrieved = templateService.getTemplate(created.getId());
        assertEquals(created.getId(), retrieved.getId());
    }

    @Test
    void testListTemplates() {
        templateService.createTemplate(DocumentTemplate.builder()
            .name("T1").defaultClassification(Document.Classification.PUBLIC).build());
        templateService.createTemplate(DocumentTemplate.builder()
            .name("T2").defaultClassification(Document.Classification.INTERNAL).build());

        Page<DocumentTemplate> page = templateService.listTemplates(PageRequest.of(0, 10));

        assertEquals(2, page.getTotalElements());
    }

    @Test
    void testUpdateTemplate() {
        DocumentTemplate template = templateService.createTemplate(DocumentTemplate.builder()
            .name("Old Name").defaultClassification(Document.Classification.PUBLIC).build());

        DocumentTemplate update = DocumentTemplate.builder()
            .name("New Name").description("Updated").defaultClassification(Document.Classification.CONFIDENTIAL).build();

        DocumentTemplate updated = templateService.updateTemplate(template.getId(), update);

        assertEquals("New Name", updated.getName());
        assertEquals("Updated", updated.getDescription());
    }

    @Test
    void testDeleteTemplate() {
        DocumentTemplate template = templateService.createTemplate(DocumentTemplate.builder()
            .name("To Delete").defaultClassification(Document.Classification.PUBLIC).build());

        templateService.deleteTemplate(template.getId());

        assertThrows(RuntimeException.class, () -> templateService.getTemplate(template.getId()));
    }

    @Test
    void testGetTemplateNotFound() {
        assertThrows(RuntimeException.class, () -> templateService.getTemplate(UUID.randomUUID()));
    }
}

package com.davidparker.dms.document.controller;

import com.davidparker.dms.document.model.DocumentTemplate;
import com.davidparker.dms.document.service.TemplateService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents/templates")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    public ResponseEntity<Page<DocumentTemplate>> listTemplates(Pageable pageable) {
        return ResponseEntity.ok(templateService.listTemplates(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentTemplate> getTemplate(@PathVariable UUID id) {
        return ResponseEntity.ok(templateService.getTemplate(id));
    }

    @PostMapping
    public ResponseEntity<DocumentTemplate> createTemplate(@RequestBody DocumentTemplate template) {
        return ResponseEntity.ok(templateService.createTemplate(template));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentTemplate> updateTemplate(@PathVariable UUID id, @RequestBody DocumentTemplate template) {
        return ResponseEntity.ok(templateService.updateTemplate(id, template));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable UUID id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
}

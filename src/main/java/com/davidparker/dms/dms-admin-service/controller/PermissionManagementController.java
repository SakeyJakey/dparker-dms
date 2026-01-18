package com.davidparker.dms.dms_admin_service.controller;

import com.davidparker.dms.dms_admin_service.dto.PermissionCreateRequest;
import com.davidparker.dms.dms_admin_service.service.PermissionManagementService;
import com.davidparker.dms.model.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/permissions")
@PreAuthorize("hasRole('DMS.Admin')")
public class PermissionManagementController {

    private final PermissionManagementService permissionManagementService;

    public PermissionManagementController(PermissionManagementService permissionManagementService) {
        this.permissionManagementService = permissionManagementService;
    }

    @GetMapping
    public ResponseEntity<Page<Permission>> listPermissions(Pageable pageable) {
        return ResponseEntity.ok(permissionManagementService.listPermissions(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Permission> getPermission(@PathVariable UUID id) {
        return ResponseEntity.ok(permissionManagementService.getPermission(id));
    }

    @PostMapping
    public ResponseEntity<Permission> createPermission(@RequestBody PermissionCreateRequest request) {
        return ResponseEntity.ok(permissionManagementService.createPermission(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermission(@PathVariable UUID id) {
        permissionManagementService.deletePermission(id);
        return ResponseEntity.noContent().build();
    }
}

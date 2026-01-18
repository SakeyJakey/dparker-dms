package com.davidparker.dms.dms_admin_service.controller;

import com.davidparker.dms.dms_admin_service.dto.RoleCreateRequest;
import com.davidparker.dms.dms_admin_service.dto.RoleUpdateRequest;
import com.davidparker.dms.dms_admin_service.service.RoleManagementService;
import com.davidparker.dms.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/roles")
@PreAuthorize("hasRole('DMS.Admin')")
public class RoleManagementController {

    private final RoleManagementService roleManagementService;

    public RoleManagementController(RoleManagementService roleManagementService) {
        this.roleManagementService = roleManagementService;
    }

    @GetMapping
    public ResponseEntity<Page<Role>> listRoles(Pageable pageable) {
        return ResponseEntity.ok(roleManagementService.listRoles(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> getRole(@PathVariable UUID id) {
        return ResponseEntity.ok(roleManagementService.getRole(id));
    }

    @PostMapping
    public ResponseEntity<Role> createRole(@RequestBody RoleCreateRequest request) {
        return ResponseEntity.ok(roleManagementService.createRole(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Role> updateRole(@PathVariable UUID id, @RequestBody RoleUpdateRequest request) {
        return ResponseEntity.ok(roleManagementService.updateRole(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID id) {
        roleManagementService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/permissions")
    public ResponseEntity<Role> assignPermission(@PathVariable UUID id, @RequestParam UUID permissionId) {
        return ResponseEntity.ok(roleManagementService.assignPermission(id, permissionId));
    }

    @DeleteMapping("/{id}/permissions/{permissionId}")
    public ResponseEntity<Role> removePermission(@PathVariable UUID id, @PathVariable UUID permissionId) {
        return ResponseEntity.ok(roleManagementService.removePermission(id, permissionId));
    }
}

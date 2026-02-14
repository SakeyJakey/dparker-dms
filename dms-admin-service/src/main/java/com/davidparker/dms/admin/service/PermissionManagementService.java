package com.davidparker.dms.admin.service;

import com.davidparker.dms.admin.dto.PermissionCreateRequest;
import com.davidparker.dms.admin.model.Permission;
import com.davidparker.dms.admin.repository.PermissionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.davidparker.dms.admin.exception.ResourceNotFoundException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PermissionManagementService {

    private final PermissionRepository permissionRepository;
    private final AuditEventClient auditEventClient;

    public PermissionManagementService(
            PermissionRepository permissionRepository,
            AuditEventClient auditEventClient) {
        this.permissionRepository = permissionRepository;
        this.auditEventClient = auditEventClient;
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    public Page<Permission> listPermissions(Pageable pageable) {
        return permissionRepository.findAll(pageable);
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    public Permission getPermission(UUID id) {
        return permissionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Permission", id.toString()));
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    @Transactional
    public Permission createPermission(PermissionCreateRequest request) {
        Permission permission = Permission.builder()
            .name(request.getName())
            .description(request.getDescription())
            .resourceType(request.getResourceType())
            .build();

        permission = permissionRepository.save(permission);

        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("eventType", "CREATE");
        auditEvent.put("eventCategory", "ADMIN_ACTIONS");
        auditEvent.put("action", "PERMISSION_CREATED");
        auditEvent.put("result", "SUCCESS");
        auditEvent.put("resourceType", "Permission");
        auditEvent.put("resourceId", permission.getId().toString());
        auditEvent.put("resourceName", permission.getName());
        auditEvent.put("timestamp", Instant.now().toString());
        auditEventClient.logEvent(auditEvent);

        return permission;
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    @Transactional
    public void deletePermission(UUID id) {
        Permission permission = permissionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Permission", id.toString()));

        permissionRepository.delete(permission);

        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("eventType", "DELETE");
        auditEvent.put("eventCategory", "ADMIN_ACTIONS");
        auditEvent.put("action", "PERMISSION_DELETED");
        auditEvent.put("result", "SUCCESS");
        auditEvent.put("resourceType", "Permission");
        auditEvent.put("resourceId", id.toString());
        auditEvent.put("timestamp", Instant.now().toString());
        auditEventClient.logEvent(auditEvent);
    }
}

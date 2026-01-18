package com.davidparker.dms.dms_admin_service.service;

import com.davidparker.dms.dms_admin_service.dto.PermissionCreateRequest;
import com.davidparker.dms.model.AuditEvent;
import com.davidparker.dms.model.Permission;
import com.davidparker.dms.repository.PermissionRepository;
import com.davidparker.dms.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PermissionManagementService {

    private final PermissionRepository permissionRepository;
    private final AuditService auditService;

    public PermissionManagementService(
            PermissionRepository permissionRepository,
            AuditService auditService) {
        this.permissionRepository = permissionRepository;
        this.auditService = auditService;
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    public Page<Permission> listPermissions(Pageable pageable) {
        return permissionRepository.findAll(pageable);
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    public Permission getPermission(UUID id) {
        return permissionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Permission not found"));
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

        auditService.logEvent(AuditEvent.builder()
            .eventType(AuditEvent.EventType.CREATE)
            .eventCategory(AuditEvent.EventCategory.ADMIN_ACTIONS)
            .action("PERMISSION_CREATED")
            .result(AuditEvent.AuditResult.SUCCESS)
            .resourceType("Permission")
            .resourceId(permission.getId())
            .resourceName(permission.getName())
            .build());

        return permission;
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    @Transactional
    public void deletePermission(UUID id) {
        Permission permission = permissionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Permission not found"));

        permissionRepository.delete(permission);

        auditService.logEvent(AuditEvent.builder()
            .eventType(AuditEvent.EventType.DELETE)
            .eventCategory(AuditEvent.EventCategory.ADMIN_ACTIONS)
            .action("PERMISSION_DELETED")
            .result(AuditEvent.AuditResult.SUCCESS)
            .resourceType("Permission")
            .resourceId(id)
            .resourceName(permission.getName())
            .build());
    }
}

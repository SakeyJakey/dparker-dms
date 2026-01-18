package com.davidparker.dms.dms_admin_service.service;

import com.davidparker.dms.dms_admin_service.dto.RoleCreateRequest;
import com.davidparker.dms.dms_admin_service.dto.RoleUpdateRequest;
import com.davidparker.dms.model.AuditEvent;
import com.davidparker.dms.model.Permission;
import com.davidparker.dms.model.Role;
import com.davidparker.dms.repository.PermissionRepository;
import com.davidparker.dms.repository.RoleRepository;
import com.davidparker.dms.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class RoleManagementService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final AuditService auditService;

    public RoleManagementService(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            AuditService auditService) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.auditService = auditService;
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    public Page<Role> listRoles(Pageable pageable) {
        return roleRepository.findAll(pageable);
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    public Role getRole(UUID id) {
        return roleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Role not found"));
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    @Transactional
    public Role createRole(RoleCreateRequest request) {
        Role role = Role.builder()
            .name(request.getName())
            .description(request.getDescription())
            .permissions(new HashSet<>())
            .build();

        if (request.getPermissionIds() != null) {
            Set<Permission> permissions = new HashSet<>(
                permissionRepository.findAllById(request.getPermissionIds()));
            role.setPermissions(permissions);
        }

        role = roleRepository.save(role);

        auditService.logEvent(AuditEvent.builder()
            .eventType(AuditEvent.EventType.CREATE)
            .eventCategory(AuditEvent.EventCategory.ADMIN_ACTIONS)
            .action("ROLE_CREATED")
            .result(AuditEvent.AuditResult.SUCCESS)
            .resourceType("Role")
            .resourceId(role.getId())
            .resourceName(role.getName())
            .build());

        return role;
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    @Transactional
    public Role updateRole(UUID id, RoleUpdateRequest request) {
        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Role not found"));

        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }

        role = roleRepository.save(role);

        auditService.logEvent(AuditEvent.builder()
            .eventType(AuditEvent.EventType.UPDATE)
            .eventCategory(AuditEvent.EventCategory.ADMIN_ACTIONS)
            .action("ROLE_UPDATED")
            .result(AuditEvent.AuditResult.SUCCESS)
            .resourceType("Role")
            .resourceId(id)
            .resourceName(role.getName())
            .build());

        return role;
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    @Transactional
    public void deleteRole(UUID id) {
        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Role not found"));

        roleRepository.delete(role);

        auditService.logEvent(AuditEvent.builder()
            .eventType(AuditEvent.EventType.DELETE)
            .eventCategory(AuditEvent.EventCategory.ADMIN_ACTIONS)
            .action("ROLE_DELETED")
            .result(AuditEvent.AuditResult.SUCCESS)
            .resourceType("Role")
            .resourceId(id)
            .resourceName(role.getName())
            .build());
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    @Transactional
    public Role assignPermission(UUID roleId, UUID permissionId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found"));
        Permission permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new RuntimeException("Permission not found"));

        role.getPermissions().add(permission);
        role = roleRepository.save(role);

        auditService.logEvent(AuditEvent.builder()
            .eventType(AuditEvent.EventType.GRANT)
            .eventCategory(AuditEvent.EventCategory.PERMISSION_CHANGES)
            .action("PERMISSION_GRANTED")
            .result(AuditEvent.AuditResult.SUCCESS)
            .resourceType("Role")
            .resourceId(roleId)
            .details(java.util.Map.of("permissionId", permissionId.toString(), "permissionName", permission.getName()))
            .build());

        return role;
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    @Transactional
    public Role removePermission(UUID roleId, UUID permissionId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found"));

        role.getPermissions().removeIf(p -> p.getId().equals(permissionId));
        role = roleRepository.save(role);

        auditService.logEvent(AuditEvent.builder()
            .eventType(AuditEvent.EventType.REVOKE)
            .eventCategory(AuditEvent.EventCategory.PERMISSION_CHANGES)
            .action("PERMISSION_REVOKED")
            .result(AuditEvent.AuditResult.SUCCESS)
            .resourceType("Role")
            .resourceId(roleId)
            .details(java.util.Map.of("permissionId", permissionId.toString()))
            .build());

        return role;
    }
}

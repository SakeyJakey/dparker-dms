package com.davidparker.dms.admin.service;

import com.davidparker.dms.admin.dto.RoleCreateRequest;
import com.davidparker.dms.admin.dto.RoleUpdateRequest;
import com.davidparker.dms.admin.model.Permission;
import com.davidparker.dms.admin.model.Role;
import com.davidparker.dms.admin.repository.PermissionRepository;
import com.davidparker.dms.admin.repository.RoleRepository;
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
public class RoleManagementService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final AuditEventClient auditEventClient;

    public RoleManagementService(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            AuditEventClient auditEventClient) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.auditEventClient = auditEventClient;
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    public Page<Role> listRoles(Pageable pageable) {
        return roleRepository.findAll(pageable);
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    public Role getRole(UUID id) {
        return roleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Role", id.toString()));
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    @Transactional
    public Role createRole(RoleCreateRequest request) {
        Role role = Role.builder()
            .name(request.getName())
            .description(request.getDescription())
            .build();

        role = roleRepository.save(role);

        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("eventType", "CREATE");
        auditEvent.put("eventCategory", "ADMIN_ACTIONS");
        auditEvent.put("action", "ROLE_CREATED");
        auditEvent.put("result", "SUCCESS");
        auditEvent.put("resourceType", "Role");
        auditEvent.put("resourceId", role.getId().toString());
        auditEvent.put("resourceName", role.getName());
        auditEvent.put("timestamp", Instant.now().toString());
        auditEventClient.logEvent(auditEvent);

        return role;
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    @Transactional
    public Role updateRole(UUID id, RoleUpdateRequest request) {
        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Role", id.toString()));

        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }

        role = roleRepository.save(role);

        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("eventType", "UPDATE");
        auditEvent.put("eventCategory", "ADMIN_ACTIONS");
        auditEvent.put("action", "ROLE_UPDATED");
        auditEvent.put("result", "SUCCESS");
        auditEvent.put("resourceType", "Role");
        auditEvent.put("resourceId", id.toString());
        auditEvent.put("timestamp", Instant.now().toString());
        auditEventClient.logEvent(auditEvent);

        return role;
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    @Transactional
    public void deleteRole(UUID id) {
        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Role", id.toString()));

        roleRepository.delete(role);

        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("eventType", "DELETE");
        auditEvent.put("eventCategory", "ADMIN_ACTIONS");
        auditEvent.put("action", "ROLE_DELETED");
        auditEvent.put("result", "SUCCESS");
        auditEvent.put("resourceType", "Role");
        auditEvent.put("resourceId", id.toString());
        auditEvent.put("timestamp", Instant.now().toString());
        auditEventClient.logEvent(auditEvent);
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    @Transactional
    public Role assignPermission(UUID roleId, UUID permissionId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role", roleId.toString()));
        Permission permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new ResourceNotFoundException("Permission", permissionId.toString()));

        role.getPermissions().add(permission);
        role = roleRepository.save(role);

        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("eventType", "GRANT");
        auditEvent.put("eventCategory", "PERMISSION_CHANGES");
        auditEvent.put("action", "PERMISSION_ASSIGNED");
        auditEvent.put("result", "SUCCESS");
        auditEvent.put("resourceType", "Role");
        auditEvent.put("resourceId", roleId.toString());
        auditEvent.put("details", Map.of("permissionId", permissionId.toString(), "permissionName", permission.getName()));
        auditEvent.put("timestamp", Instant.now().toString());
        auditEventClient.logEvent(auditEvent);

        return role;
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    @Transactional
    public Role removePermission(UUID roleId, UUID permissionId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role", roleId.toString()));

        role.getPermissions().removeIf(p -> p.getId().equals(permissionId));
        role = roleRepository.save(role);

        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("eventType", "REVOKE");
        auditEvent.put("eventCategory", "PERMISSION_CHANGES");
        auditEvent.put("action", "PERMISSION_REVOKED");
        auditEvent.put("result", "SUCCESS");
        auditEvent.put("resourceType", "Role");
        auditEvent.put("resourceId", roleId.toString());
        auditEvent.put("timestamp", Instant.now().toString());
        auditEventClient.logEvent(auditEvent);

        return role;
    }
}

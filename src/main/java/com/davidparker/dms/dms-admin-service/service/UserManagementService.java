package com.davidparker.dms.dms_admin_service.service;

import com.davidparker.dms.dms_admin_service.dto.UserCreateRequest;
import com.davidparker.dms.dms_admin_service.dto.UserUpdateRequest;
import com.davidparker.dms.model.AuditEvent;
import com.davidparker.dms.model.User;
import com.davidparker.dms.repository.RoleRepository;
import com.davidparker.dms.repository.UserRepository;
import com.davidparker.dms.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuditService auditService;

    public UserManagementService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            AuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.auditService = auditService;
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    public Page<User> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    public User getUser(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    @Transactional
    public User createUser(UserCreateRequest request) {
        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .displayName(request.getDisplayName())
            .enabled(true)
            .build();

        user = userRepository.save(user);

        auditService.logEvent(AuditEvent.builder()
            .eventType(AuditEvent.EventType.USER_CREATE)
            .eventCategory(AuditEvent.EventCategory.ADMIN_ACTIONS)
            .action("USER_CREATED")
            .result(AuditEvent.AuditResult.SUCCESS)
            .resourceType("User")
            .resourceId(user.getId())
            .resourceName(user.getUsername())
            .build());

        return user;
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    @Transactional
    public User updateUser(UUID id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }

        user = userRepository.save(user);

        auditService.logEvent(AuditEvent.builder()
            .eventType(AuditEvent.EventType.UPDATE)
            .eventCategory(AuditEvent.EventCategory.ADMIN_ACTIONS)
            .action("USER_UPDATED")
            .result(AuditEvent.AuditResult.SUCCESS)
            .resourceType("User")
            .resourceId(id)
            .resourceName(user.getUsername())
            .build());

        return user;
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.delete(user);

        auditService.logEvent(AuditEvent.builder()
            .eventType(AuditEvent.EventType.DELETE)
            .eventCategory(AuditEvent.EventCategory.ADMIN_ACTIONS)
            .action("USER_DELETED")
            .result(AuditEvent.AuditResult.SUCCESS)
            .resourceType("User")
            .resourceId(id)
            .resourceName(user.getUsername())
            .build());
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    @Transactional
    public User assignRole(UUID userId, UUID roleId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        var role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found"));

        user.getRoles().add(role);
        user = userRepository.save(user);

        auditService.logEvent(AuditEvent.builder()
            .eventType(AuditEvent.EventType.ROLE_ASSIGN)
            .eventCategory(AuditEvent.EventCategory.PERMISSION_CHANGES)
            .action("ROLE_ASSIGNED")
            .result(AuditEvent.AuditResult.SUCCESS)
            .resourceType("User")
            .resourceId(userId)
            .details(java.util.Map.of("roleId", roleId.toString(), "roleName", role.getName()))
            .build());

        return user;
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    @Transactional
    public User removeRole(UUID userId, UUID roleId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        user.getRoles().removeIf(role -> role.getId().equals(roleId));
        user = userRepository.save(user);

        auditService.logEvent(AuditEvent.builder()
            .eventType(AuditEvent.EventType.REVOKE)
            .eventCategory(AuditEvent.EventCategory.PERMISSION_CHANGES)
            .action("ROLE_REVOKED")
            .result(AuditEvent.AuditResult.SUCCESS)
            .resourceType("User")
            .resourceId(userId)
            .details(java.util.Map.of("roleId", roleId.toString()))
            .build());

        return user;
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    @Transactional
    public User enableUser(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(true);
        return userRepository.save(user);
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    @Transactional
    public User disableUser(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(false);
        return userRepository.save(user);
    }
}

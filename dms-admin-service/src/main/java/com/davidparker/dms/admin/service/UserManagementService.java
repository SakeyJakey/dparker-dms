package com.davidparker.dms.admin.service;

import com.davidparker.dms.admin.dto.UserCreateRequest;
import com.davidparker.dms.admin.dto.UserUpdateRequest;
import com.davidparker.dms.admin.model.Role;
import com.davidparker.dms.admin.model.User;
import com.davidparker.dms.admin.repository.RoleRepository;
import com.davidparker.dms.admin.repository.UserRepository;
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
public class UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuditEventClient auditEventClient;

    public UserManagementService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            AuditEventClient auditEventClient) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.auditEventClient = auditEventClient;
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    public Page<User> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    public User getUser(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));
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

        // Log audit event
        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("eventType", "USER_CREATE");
        auditEvent.put("eventCategory", "ADMIN_ACTIONS");
        auditEvent.put("action", "USER_CREATED");
        auditEvent.put("result", "SUCCESS");
        auditEvent.put("resourceType", "User");
        auditEvent.put("resourceId", user.getId().toString());
        auditEvent.put("resourceName", user.getUsername());
        auditEvent.put("timestamp", Instant.now().toString());
        auditEventClient.logEvent(auditEvent);

        return user;
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    @Transactional
    public User updateUser(UUID id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }

        user = userRepository.save(user);

        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("eventType", "UPDATE");
        auditEvent.put("eventCategory", "ADMIN_ACTIONS");
        auditEvent.put("action", "USER_UPDATED");
        auditEvent.put("result", "SUCCESS");
        auditEvent.put("resourceType", "User");
        auditEvent.put("resourceId", id.toString());
        auditEvent.put("timestamp", Instant.now().toString());
        auditEventClient.logEvent(auditEvent);

        return user;
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));

        userRepository.delete(user);

        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("eventType", "DELETE");
        auditEvent.put("eventCategory", "ADMIN_ACTIONS");
        auditEvent.put("action", "USER_DELETED");
        auditEvent.put("result", "SUCCESS");
        auditEvent.put("resourceType", "User");
        auditEvent.put("resourceId", id.toString());
        auditEvent.put("timestamp", Instant.now().toString());
        auditEventClient.logEvent(auditEvent);
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    @Transactional
    public User assignRole(UUID userId, UUID roleId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role", roleId.toString()));

        user.getRoles().add(role);
        user = userRepository.save(user);

        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("eventType", "ROLE_ASSIGN");
        auditEvent.put("eventCategory", "PERMISSION_CHANGES");
        auditEvent.put("action", "ROLE_ASSIGNED");
        auditEvent.put("result", "SUCCESS");
        auditEvent.put("resourceType", "User");
        auditEvent.put("resourceId", userId.toString());
        auditEvent.put("details", Map.of("roleId", roleId.toString(), "roleName", role.getName()));
        auditEvent.put("timestamp", Instant.now().toString());
        auditEventClient.logEvent(auditEvent);

        return user;
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    @Transactional
    public User removeRole(UUID userId, UUID roleId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        user.getRoles().removeIf(role -> role.getId().equals(roleId));
        user = userRepository.save(user);

        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("eventType", "REVOKE");
        auditEvent.put("eventCategory", "PERMISSION_CHANGES");
        auditEvent.put("action", "ROLE_REVOKED");
        auditEvent.put("result", "SUCCESS");
        auditEvent.put("resourceType", "User");
        auditEvent.put("resourceId", userId.toString());
        auditEvent.put("timestamp", Instant.now().toString());
        auditEventClient.logEvent(auditEvent);

        return user;
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    @Transactional
    public User enableUser(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));
        user.setEnabled(true);
        return userRepository.save(user);
    }

    @PreAuthorize("hasRole('DMS.Admin')")
    @Transactional
    public User disableUser(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));
        user.setEnabled(false);
        return userRepository.save(user);
    }
}

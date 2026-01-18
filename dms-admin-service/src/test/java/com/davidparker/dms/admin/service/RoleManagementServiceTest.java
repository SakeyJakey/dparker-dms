package com.davidparker.dms.admin.service;

import com.davidparker.dms.admin.dto.RoleCreateRequest;
import com.davidparker.dms.admin.dto.RoleUpdateRequest;
import com.davidparker.dms.admin.model.Permission;
import com.davidparker.dms.admin.model.Role;
import com.davidparker.dms.admin.repository.PermissionRepository;
import com.davidparker.dms.admin.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleManagementServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private AuditEventClient auditEventClient;

    @InjectMocks
    private RoleManagementService roleManagementService;

    private Role testRole;
    private Permission testPermission;
    private UUID roleId;
    private UUID permissionId;

    @BeforeEach
    void setUp() {
        roleId = UUID.randomUUID();
        permissionId = UUID.randomUUID();

        testRole = Role.builder()
            .id(roleId)
            .name("TEST_ROLE")
            .description("Test Role")
            .permissions(new HashSet<>())
            .createdAt(Instant.now())
            .build();

        testPermission = Permission.builder()
            .id(permissionId)
            .name("TEST_PERMISSION")
            .description("Test Permission")
            .resourceType("Document")
            .createdAt(Instant.now())
            .build();
    }

    @Test
    void testListRoles() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Role> rolePage = new PageImpl<>(List.of(testRole), pageable, 1);

        when(roleRepository.findAll(pageable)).thenReturn(rolePage);

        Page<Role> result = roleManagementService.listRoles(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testRole, result.getContent().get(0));
        verify(roleRepository).findAll(pageable);
    }

    @Test
    void testGetRole_Success() {
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(testRole));

        Role result = roleManagementService.getRole(roleId);

        assertNotNull(result);
        assertEquals(testRole.getId(), result.getId());
        assertEquals(testRole.getName(), result.getName());
        verify(roleRepository).findById(roleId);
    }

    @Test
    void testGetRole_NotFound() {
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> roleManagementService.getRole(roleId));
        verify(roleRepository).findById(roleId);
    }

    @Test
    void testCreateRole() {
        RoleCreateRequest request = new RoleCreateRequest();
        request.setApplicationName("NEW_ROLE");
        request.setDescription("New Role Description");

        when(roleRepository.save(any(Role.class))).thenReturn(testRole);
        doNothing().when(auditEventClient).logEvent(any());

        Role result = roleManagementService.createRole(request);

        assertNotNull(result);
        verify(roleRepository).save(any(Role.class));
        verify(auditEventClient).logEvent(any());
    }

    @Test
    void testUpdateRole() {
        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setDescription("Updated Description");

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(testRole));
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);
        doNothing().when(auditEventClient).logEvent(any());

        Role result = roleManagementService.updateRole(roleId, request);

        assertNotNull(result);
        verify(roleRepository).findById(roleId);
        verify(roleRepository).save(any(Role.class));
        verify(auditEventClient).logEvent(any());
    }

    @Test
    void testDeleteRole() {
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(testRole));
        doNothing().when(roleRepository).delete(testRole);
        doNothing().when(auditEventClient).logEvent(any());

        roleManagementService.deleteRole(roleId);

        verify(roleRepository).findById(roleId);
        verify(roleRepository).delete(testRole);
        verify(auditEventClient).logEvent(any());
    }

    @Test
    void testAssignPermission() {
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(testRole));
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(testPermission));
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);
        doNothing().when(auditEventClient).logEvent(any());

        Role result = roleManagementService.assignPermission(roleId, permissionId);

        assertNotNull(result);
        verify(roleRepository).findById(roleId);
        verify(permissionRepository).findById(permissionId);
        verify(roleRepository).save(any(Role.class));
        verify(auditEventClient).logEvent(any());
    }

    @Test
    void testRemovePermission() {
        testRole.getPermissions().add(testPermission);
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(testRole));
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);
        doNothing().when(auditEventClient).logEvent(any());

        Role result = roleManagementService.removePermission(roleId, permissionId);

        assertNotNull(result);
        verify(roleRepository).findById(roleId);
        verify(roleRepository).save(any(Role.class));
        verify(auditEventClient).logEvent(any());
    }
}

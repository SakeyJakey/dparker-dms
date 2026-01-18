package com.davidparker.dms.admin.service;

import com.davidparker.dms.admin.dto.PermissionCreateRequest;
import com.davidparker.dms.admin.model.Permission;
import com.davidparker.dms.admin.repository.PermissionRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionManagementServiceTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private AuditEventClient auditEventClient;

    @InjectMocks
    private PermissionManagementService permissionManagementService;

    private Permission testPermission;
    private UUID permissionId;

    @BeforeEach
    void setUp() {
        permissionId = UUID.randomUUID();

        testPermission = Permission.builder()
            .id(permissionId)
            .applicationName("TEST_PERMISSION")
            .description("Test Permission")
            .resourceType("Document")
            .createdAt(Instant.now())
            .build();
    }

    @Test
    void testListPermissions() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Permission> permissionPage = new PageImpl<>(List.of(testPermission), pageable, 1);

        when(permissionRepository.findAll(pageable)).thenReturn(permissionPage);

        Page<Permission> result = permissionManagementService.listPermissions(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testPermission, result.getContent().get(0));
        verify(permissionRepository).findAll(pageable);
    }

    @Test
    void testGetPermission_Success() {
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(testPermission));

        Permission result = permissionManagementService.getPermission(permissionId);

        assertNotNull(result);
        assertEquals(testPermission.getId(), result.getId());
        assertEquals(testPermission.getName(), result.getName());
        verify(permissionRepository).findById(permissionId);
    }

    @Test
    void testGetPermission_NotFound() {
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> permissionManagementService.getPermission(permissionId));
        verify(permissionRepository).findById(permissionId);
    }

    @Test
    void testCreatePermission() {
        PermissionCreateRequest request = new PermissionCreateRequest();
        request.setApplicationName("NEW_PERMISSION");
        request.setDescription("New Permission Description");
        request.setResourceType("Document");

        when(permissionRepository.save(any(Permission.class))).thenReturn(testPermission);
        doNothing().when(auditEventClient).logEvent(any());

        Permission result = permissionManagementService.createPermission(request);

        assertNotNull(result);
        verify(permissionRepository).save(any(Permission.class));
        verify(auditEventClient).logEvent(any());
    }

    @Test
    void testDeletePermission() {
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(testPermission));
        doNothing().when(permissionRepository).delete(testPermission);
        doNothing().when(auditEventClient).logEvent(any());

        permissionManagementService.deletePermission(permissionId);

        verify(permissionRepository).findById(permissionId);
        verify(permissionRepository).delete(testPermission);
        verify(auditEventClient).logEvent(any());
    }
}

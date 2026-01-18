package com.davidparker.dms.admin.service;

import com.davidparker.dms.admin.dto.UserCreateRequest;
import com.davidparker.dms.admin.dto.UserUpdateRequest;
import com.davidparker.dms.admin.model.Role;
import com.davidparker.dms.admin.model.User;
import com.davidparker.dms.admin.repository.RoleRepository;
import com.davidparker.dms.admin.repository.UserRepository;
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
class UserManagementServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AuditEventClient auditEventClient;

    @InjectMocks
    private UserManagementService userManagementService;

    private User testUser;
    private Role testRole;
    private UUID userId;
    private UUID roleId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        roleId = UUID.randomUUID();

        testUser = User.builder()
            .id(userId)
            .username("testuser")
            .email("test@example.com")
            .displayName("Test User")
            .enabled(true)
            .roles(new HashSet<>())
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        testRole = Role.builder()
            .id(roleId)
            .applicationName("TEST_ROLE")
            .description("Test Role")
            .permissions(new HashSet<>())
            .createdAt(Instant.now())
            .build();
    }

    @Test
    void testListUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        Page<User> result = userManagementService.listUsers(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testUser, result.getContent().get(0));
        verify(userRepository).findAll(pageable);
    }

    @Test
    void testGetUser_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        User result = userManagementService.getUser(userId);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
        verify(userRepository).findById(userId);
    }

    @Test
    void testGetUser_NotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userManagementService.getUser(userId));
        verify(userRepository).findById(userId);
    }

    @Test
    void testCreateUser() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setDisplayName("New User");

        User newUser = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .displayName(request.getDisplayName())
            .enabled(true)
            .build();

        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(auditEventClient).logEvent(any());

        User result = userManagementService.createUser(request);

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
        verify(auditEventClient).logEvent(any());
    }

    @Test
    void testUpdateUser_Success() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("updated@example.com");
        request.setDisplayName("Updated User");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(auditEventClient).logEvent(any());

        User result = userManagementService.updateUser(userId, request);

        assertNotNull(result);
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
        verify(auditEventClient).logEvent(any());
    }

    @Test
    void testUpdateUser_NotFound() {
        UserUpdateRequest request = new UserUpdateRequest();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userManagementService.updateUser(userId, request));
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void testDeleteUser() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);
        doNothing().when(auditEventClient).logEvent(any());

        userManagementService.deleteUser(userId);

        verify(userRepository).findById(userId);
        verify(userRepository).delete(testUser);
        verify(auditEventClient).logEvent(any());
    }

    @Test
    void testAssignRole() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(testRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(auditEventClient).logEvent(any());

        User result = userManagementService.assignRole(userId, roleId);

        assertNotNull(result);
        verify(userRepository).findById(userId);
        verify(roleRepository).findById(roleId);
        verify(userRepository).save(any(User.class));
        verify(auditEventClient).logEvent(any());
    }

    @Test
    void testRemoveRole() {
        testUser.getRoles().add(testRole);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(auditEventClient).logEvent(any());

        User result = userManagementService.removeRole(userId, roleId);

        assertNotNull(result);
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
        verify(auditEventClient).logEvent(any());
    }

    @Test
    void testEnableUser() {
        testUser.setEnabled(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userManagementService.enableUser(userId);

        assertNotNull(result);
        assertTrue(result.getEnabled());
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testDisableUser() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userManagementService.disableUser(userId);

        assertNotNull(result);
        assertFalse(result.getEnabled());
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }
}

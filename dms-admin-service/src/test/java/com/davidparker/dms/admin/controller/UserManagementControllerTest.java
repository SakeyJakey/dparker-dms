package com.davidparker.dms.admin.controller;

import com.davidparker.dms.admin.dto.UserCreateRequest;
import com.davidparker.dms.admin.dto.UserUpdateRequest;
import com.davidparker.dms.admin.model.User;
import com.davidparker.dms.admin.service.UserManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserManagementController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {UserManagementController.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.cloud.azure.keyvault.secret.enabled=false",
    "spring.cloud.azure.keyvault.secret.property-sources[0].enabled=false"
})
class UserManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserManagementService userManagementService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UUID userId = UUID.randomUUID();

    private User createTestUser() {
        return User.builder()
            .id(userId)
            .username("testuser")
            .email("test@example.com")
            .displayName("Test User")
            .enabled(true)
            .roles(new HashSet<>())
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    }

    @Test
    void testListUsers() throws Exception {
        User user = createTestUser();
        Page<User> userPage = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);
        when(userManagementService.listUsers(any())).thenReturn(userPage);

        mockMvc.perform(get("/api/v1/admin/users").param("page", "0").param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].username").value("testuser"));

        verify(userManagementService).listUsers(any());
    }

    @Test
    void testGetUser() throws Exception {
        User user = createTestUser();
        when(userManagementService.getUser(userId)).thenReturn(user);

        mockMvc.perform(get("/api/v1/admin/users/{id}", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("testuser"));

        verify(userManagementService).getUser(userId);
    }

    @Test
    void testCreateUser() throws Exception {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setDisplayName("New User");

        User createdUser = createTestUser();
        when(userManagementService.createUser(any(UserCreateRequest.class))).thenReturn(createdUser);

        mockMvc.perform(post("/api/v1/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("testuser"));

        verify(userManagementService).createUser(any(UserCreateRequest.class));
    }

    @Test
    void testUpdateUser() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("updated@example.com");
        request.setDisplayName("Updated User");

        User updatedUser = createTestUser();
        when(userManagementService.updateUser(eq(userId), any(UserUpdateRequest.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/v1/admin/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        verify(userManagementService).updateUser(eq(userId), any(UserUpdateRequest.class));
    }

    @Test
    void testDeleteUser() throws Exception {
        doNothing().when(userManagementService).deleteUser(userId);

        mockMvc.perform(delete("/api/v1/admin/users/{id}", userId))
            .andExpect(status().isNoContent());

        verify(userManagementService).deleteUser(userId);
    }

    @Test
    void testAssignRole() throws Exception {
        UUID roleId = UUID.randomUUID();
        User user = createTestUser();
        when(userManagementService.assignRole(userId, roleId)).thenReturn(user);

        mockMvc.perform(post("/api/v1/admin/users/{id}/roles", userId)
                .param("roleId", roleId.toString()))
            .andExpect(status().isOk());

        verify(userManagementService).assignRole(userId, roleId);
    }

    @Test
    void testRemoveRole() throws Exception {
        UUID roleId = UUID.randomUUID();
        User user = createTestUser();
        when(userManagementService.removeRole(userId, roleId)).thenReturn(user);

        mockMvc.perform(delete("/api/v1/admin/users/{id}/roles/{roleId}", userId, roleId))
            .andExpect(status().isOk());

        verify(userManagementService).removeRole(userId, roleId);
    }

    @Test
    void testEnableUser() throws Exception {
        User user = createTestUser();
        when(userManagementService.enableUser(userId)).thenReturn(user);

        mockMvc.perform(put("/api/v1/admin/users/{id}/enable", userId))
            .andExpect(status().isOk());

        verify(userManagementService).enableUser(userId);
    }

    @Test
    void testDisableUser() throws Exception {
        User user = createTestUser();
        when(userManagementService.disableUser(userId)).thenReturn(user);

        mockMvc.perform(put("/api/v1/admin/users/{id}/disable", userId))
            .andExpect(status().isOk());

        verify(userManagementService).disableUser(userId);
    }
}

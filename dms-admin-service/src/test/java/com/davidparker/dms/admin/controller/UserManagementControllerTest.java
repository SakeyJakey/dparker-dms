package com.davidparker.dms.admin.controller;

import com.davidparker.dms.admin.dto.UserCreateRequest;
import com.davidparker.dms.admin.dto.UserUpdateRequest;
import com.davidparker.dms.admin.model.User;
import com.davidparker.dms.admin.service.UserManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserManagementController.class)
class UserManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserManagementService userManagementService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId = UUID.randomUUID();

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testListUsers() throws Exception {
        User user = User.builder()
            .id(userId)
            .username("testuser")
            .email("test@example.com")
            .displayName("Test User")
            .enabled(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        Page<User> userPage = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);

        when(userManagementService.listUsers(any())).thenReturn(userPage);

        mockMvc.perform(get("/api/v1/admin/users")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].username").value("testuser"));

        verify(userManagementService).listUsers(any());
    }

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testGetUser() throws Exception {
        User user = User.builder()
            .id(userId)
            .username("testuser")
            .email("test@example.com")
            .displayName("Test User")
            .enabled(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        when(userManagementService.getUser(userId)).thenReturn(user);

        mockMvc.perform(get("/api/v1/admin/users/{id}", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(userId.toString()))
            .andExpect(jsonPath("$.username").value("testuser"));

        verify(userManagementService).getUser(userId);
    }

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testCreateUser() throws Exception {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setDisplayName("New User");

        User createdUser = User.builder()
            .id(userId)
            .username(request.getUsername())
            .email(request.getEmail())
            .displayName(request.getDisplayName())
            .enabled(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        when(userManagementService.createUser(any(UserCreateRequest.class))).thenReturn(createdUser);

        mockMvc.perform(post("/api/v1/admin/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("newuser"));

        verify(userManagementService).createUser(any(UserCreateRequest.class));
    }

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testUpdateUser() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("updated@example.com");
        request.setDisplayName("Updated User");

        User updatedUser = User.builder()
            .id(userId)
            .username("testuser")
            .email(request.getEmail())
            .displayName(request.getDisplayName())
            .enabled(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        when(userManagementService.updateUser(eq(userId), any(UserUpdateRequest.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/v1/admin/users/{id}", userId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("updated@example.com"));

        verify(userManagementService).updateUser(eq(userId), any(UserUpdateRequest.class));
    }

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testDeleteUser() throws Exception {
        doNothing().when(userManagementService).deleteUser(userId);

        mockMvc.perform(delete("/api/v1/admin/users/{id}", userId)
                .with(csrf()))
            .andExpect(status().isNoContent());

        verify(userManagementService).deleteUser(userId);
    }

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testAssignRole() throws Exception {
        UUID roleId = UUID.randomUUID();
        User user = User.builder()
            .id(userId)
            .username("testuser")
            .email("test@example.com")
            .enabled(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        when(userManagementService.assignRole(userId, roleId)).thenReturn(user);

        mockMvc.perform(post("/api/v1/admin/users/{id}/roles", userId)
                .with(csrf())
                .param("roleId", roleId.toString()))
            .andExpect(status().isOk());

        verify(userManagementService).assignRole(userId, roleId);
    }

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testRemoveRole() throws Exception {
        UUID roleId = UUID.randomUUID();
        User user = User.builder()
            .id(userId)
            .username("testuser")
            .email("test@example.com")
            .enabled(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        when(userManagementService.removeRole(userId, roleId)).thenReturn(user);

        mockMvc.perform(delete("/api/v1/admin/users/{id}/roles/{roleId}", userId, roleId)
                .with(csrf()))
            .andExpect(status().isOk());

        verify(userManagementService).removeRole(userId, roleId);
    }

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testEnableUser() throws Exception {
        User user = User.builder()
            .id(userId)
            .username("testuser")
            .email("test@example.com")
            .enabled(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        when(userManagementService.enableUser(userId)).thenReturn(user);

        mockMvc.perform(put("/api/v1/admin/users/{id}/enable", userId)
                .with(csrf()))
            .andExpect(status().isOk());

        verify(userManagementService).enableUser(userId);
    }

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testDisableUser() throws Exception {
        User user = User.builder()
            .id(userId)
            .username("testuser")
            .email("test@example.com")
            .enabled(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        when(userManagementService.disableUser(userId)).thenReturn(user);

        mockMvc.perform(put("/api/v1/admin/users/{id}/disable", userId)
                .with(csrf()))
            .andExpect(status().isOk());

        verify(userManagementService).disableUser(userId);
    }
}

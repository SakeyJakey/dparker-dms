package com.davidparker.dms.admin.controller;

import com.davidparker.dms.admin.dto.RoleCreateRequest;
import com.davidparker.dms.admin.dto.RoleUpdateRequest;
import com.davidparker.dms.admin.model.Role;
import com.davidparker.dms.admin.service.RoleManagementService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoleManagementController.class)
class RoleManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoleManagementService roleManagementService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID roleId = UUID.randomUUID();

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testListRoles() throws Exception {
        Role role = Role.builder()
            .id(roleId)
            .name("Test Role")
            .description("Test Description")
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        Page<Role> rolePage = new PageImpl<>(List.of(role), PageRequest.of(0, 10), 1);

        when(roleManagementService.listRoles(any())).thenReturn(rolePage);

        mockMvc.perform(get("/api/v1/admin/roles")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].name").value("Test Role"));

        verify(roleManagementService).listRoles(any());
    }

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testGetRole() throws Exception {
        Role role = Role.builder()
            .id(roleId)
            .name("Test Role")
            .description("Test Description")
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        when(roleManagementService.getRole(roleId)).thenReturn(role);

        mockMvc.perform(get("/api/v1/admin/roles/{id}", roleId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(roleId.toString()))
            .andExpect(jsonPath("$.name").value("Test Role"));

        verify(roleManagementService).getRole(roleId);
    }

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testCreateRole() throws Exception {
        RoleCreateRequest request = new RoleCreateRequest();
        request.setName("New Role");
        request.setDescription("New Description");

        Role createdRole = Role.builder()
            .id(roleId)
            .name(request.getName())
            .description(request.getDescription())
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        when(roleManagementService.createRole(any(RoleCreateRequest.class))).thenReturn(createdRole);

        mockMvc.perform(post("/api/v1/admin/roles")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("New Role"));

        verify(roleManagementService).createRole(any(RoleCreateRequest.class));
    }

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testUpdateRole() throws Exception {
        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setDescription("Updated Description");

        Role updatedRole = Role.builder()
            .id(roleId)
            .name("Test Role")
            .description(request.getDescription())
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        when(roleManagementService.updateRole(eq(roleId), any(RoleUpdateRequest.class))).thenReturn(updatedRole);

        mockMvc.perform(put("/api/v1/admin/roles/{id}", roleId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.description").value("Updated Description"));

        verify(roleManagementService).updateRole(eq(roleId), any(RoleUpdateRequest.class));
    }

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testDeleteRole() throws Exception {
        doNothing().when(roleManagementService).deleteRole(roleId);

        mockMvc.perform(delete("/api/v1/admin/roles/{id}", roleId)
                .with(csrf()))
            .andExpect(status().isNoContent());

        verify(roleManagementService).deleteRole(roleId);
    }
}

package com.davidparker.dms.admin.controller;

import com.davidparker.dms.admin.dto.RoleCreateRequest;
import com.davidparker.dms.admin.dto.RoleUpdateRequest;
import com.davidparker.dms.admin.model.Role;
import com.davidparker.dms.admin.service.RoleManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RoleManagementController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {RoleManagementController.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.cloud.azure.keyvault.secret.enabled=false",
    "spring.cloud.azure.keyvault.secret.property-sources[0].enabled=false"
})
class RoleManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoleManagementService roleManagementService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UUID roleId = UUID.randomUUID();

    private Role createTestRole() {
        return Role.builder()
            .id(roleId)
            .name("TEST_ROLE")
            .description("Test Role")
            .permissions(new HashSet<>())
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    }

    @Test
    void testListRoles() throws Exception {
        Role role = createTestRole();
        Page<Role> rolePage = new PageImpl<>(List.of(role), PageRequest.of(0, 10), 1);
        when(roleManagementService.listRoles(any())).thenReturn(rolePage);

        mockMvc.perform(get("/api/v1/admin/roles").param("page", "0").param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());

        verify(roleManagementService).listRoles(any());
    }

    @Test
    void testGetRole() throws Exception {
        Role role = createTestRole();
        when(roleManagementService.getRole(roleId)).thenReturn(role);

        mockMvc.perform(get("/api/v1/admin/roles/{id}", roleId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("TEST_ROLE"));

        verify(roleManagementService).getRole(roleId);
    }

    @Test
    void testCreateRole() throws Exception {
        RoleCreateRequest request = new RoleCreateRequest();
        request.setName("NEW_ROLE");
        request.setDescription("New Role");

        Role createdRole = createTestRole();
        when(roleManagementService.createRole(any(RoleCreateRequest.class))).thenReturn(createdRole);

        mockMvc.perform(post("/api/v1/admin/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        verify(roleManagementService).createRole(any(RoleCreateRequest.class));
    }

    @Test
    void testUpdateRole() throws Exception {
        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setDescription("Updated Description");

        Role updatedRole = createTestRole();
        when(roleManagementService.updateRole(eq(roleId), any(RoleUpdateRequest.class))).thenReturn(updatedRole);

        mockMvc.perform(put("/api/v1/admin/roles/{id}", roleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        verify(roleManagementService).updateRole(eq(roleId), any(RoleUpdateRequest.class));
    }

    @Test
    void testDeleteRole() throws Exception {
        doNothing().when(roleManagementService).deleteRole(roleId);

        mockMvc.perform(delete("/api/v1/admin/roles/{id}", roleId))
            .andExpect(status().isNoContent());

        verify(roleManagementService).deleteRole(roleId);
    }

    @Test
    void testAssignPermission() throws Exception {
        UUID permissionId = UUID.randomUUID();
        Role role = createTestRole();
        when(roleManagementService.assignPermission(roleId, permissionId)).thenReturn(role);

        mockMvc.perform(post("/api/v1/admin/roles/{id}/permissions", roleId)
                .param("permissionId", permissionId.toString()))
            .andExpect(status().isOk());

        verify(roleManagementService).assignPermission(roleId, permissionId);
    }

    @Test
    void testRemovePermission() throws Exception {
        UUID permissionId = UUID.randomUUID();
        Role role = createTestRole();
        when(roleManagementService.removePermission(roleId, permissionId)).thenReturn(role);

        mockMvc.perform(delete("/api/v1/admin/roles/{id}/permissions/{permissionId}", roleId, permissionId))
            .andExpect(status().isOk());

        verify(roleManagementService).removePermission(roleId, permissionId);
    }
}

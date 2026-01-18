package com.davidparker.dms.admin.controller;

import com.davidparker.dms.admin.dto.PermissionCreateRequest;
import com.davidparker.dms.admin.model.Permission;
import com.davidparker.dms.admin.service.PermissionManagementService;
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

@WebMvcTest(PermissionManagementController.class)
class PermissionManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PermissionManagementService permissionManagementService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID permissionId = UUID.randomUUID();

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testListPermissions() throws Exception {
        Permission permission = Permission.builder()
            .id(permissionId)
            .name("document.read")
            .description("Read documents")
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        Page<Permission> permissionPage = new PageImpl<>(List.of(permission), PageRequest.of(0, 10), 1);

        when(permissionManagementService.listPermissions(any())).thenReturn(permissionPage);

        mockMvc.perform(get("/api/v1/admin/permissions")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].name").value("document.read"));

        verify(permissionManagementService).listPermissions(any());
    }

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testGetPermission() throws Exception {
        Permission permission = Permission.builder()
            .id(permissionId)
            .name("document.read")
            .description("Read documents")
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        when(permissionManagementService.getPermission(permissionId)).thenReturn(permission);

        mockMvc.perform(get("/api/v1/admin/permissions/{id}", permissionId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(permissionId.toString()))
            .andExpect(jsonPath("$.name").value("document.read"));

        verify(permissionManagementService).getPermission(permissionId);
    }

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testCreatePermission() throws Exception {
        PermissionCreateRequest request = new PermissionCreateRequest();
        request.setName("document.write");
        request.setDescription("Write documents");

        Permission createdPermission = Permission.builder()
            .id(permissionId)
            .name(request.getName())
            .description(request.getDescription())
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        when(permissionManagementService.createPermission(any(PermissionCreateRequest.class)))
            .thenReturn(createdPermission);

        mockMvc.perform(post("/api/v1/admin/permissions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("document.write"));

        verify(permissionManagementService).createPermission(any(PermissionCreateRequest.class));
    }

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testDeletePermission() throws Exception {
        doNothing().when(permissionManagementService).deletePermission(permissionId);

        mockMvc.perform(delete("/api/v1/admin/permissions/{id}", permissionId)
                .with(csrf()))
            .andExpect(status().isNoContent());

        verify(permissionManagementService).deletePermission(permissionId);
    }
}

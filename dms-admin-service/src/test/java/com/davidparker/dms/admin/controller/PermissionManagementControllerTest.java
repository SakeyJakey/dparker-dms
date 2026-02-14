package com.davidparker.dms.admin.controller;

import com.davidparker.dms.admin.dto.PermissionCreateRequest;
import com.davidparker.dms.admin.model.Permission;
import com.davidparker.dms.admin.service.PermissionManagementService;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PermissionManagementController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {PermissionManagementController.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.cloud.azure.keyvault.secret.enabled=false",
    "spring.cloud.azure.keyvault.secret.property-sources[0].enabled=false"
})
class PermissionManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PermissionManagementService permissionManagementService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UUID permissionId = UUID.randomUUID();

    private Permission createTestPermission() {
        return Permission.builder()
            .id(permissionId)
            .name("documents.read")
            .description("Read documents")
            .resourceType("Document")
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    }

    @Test
    void testListPermissions() throws Exception {
        Permission permission = createTestPermission();
        Page<Permission> page = new PageImpl<>(List.of(permission), PageRequest.of(0, 10), 1);
        when(permissionManagementService.listPermissions(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/permissions").param("page", "0").param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());

        verify(permissionManagementService).listPermissions(any());
    }

    @Test
    void testGetPermission() throws Exception {
        Permission permission = createTestPermission();
        when(permissionManagementService.getPermission(permissionId)).thenReturn(permission);

        mockMvc.perform(get("/api/v1/admin/permissions/{id}", permissionId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("documents.read"));

        verify(permissionManagementService).getPermission(permissionId);
    }

    @Test
    void testCreatePermission() throws Exception {
        PermissionCreateRequest request = new PermissionCreateRequest();
        request.setName("documents.write");
        request.setDescription("Write documents");
        request.setResourceType("Document");

        Permission created = createTestPermission();
        when(permissionManagementService.createPermission(any(PermissionCreateRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/v1/admin/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        verify(permissionManagementService).createPermission(any(PermissionCreateRequest.class));
    }

    @Test
    void testDeletePermission() throws Exception {
        doNothing().when(permissionManagementService).deletePermission(permissionId);

        mockMvc.perform(delete("/api/v1/admin/permissions/{id}", permissionId))
            .andExpect(status().isNoContent());

        verify(permissionManagementService).deletePermission(permissionId);
    }
}

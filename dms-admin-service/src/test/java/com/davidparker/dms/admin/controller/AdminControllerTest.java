package com.davidparker.dms.admin.controller;

import com.davidparker.dms.admin.config.CorsConfig;
import com.davidparker.dms.admin.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = AdminController.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.cloud.azure.keyvault.secret.enabled=false",
    "spring.cloud.azure.keyvault.secret.property-sources[0].enabled=false"
})
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("operational"))
            .andExpect(jsonPath("$.service").value("dms-admin-service"));
    }
}

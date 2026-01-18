package com.davidparker.dms.admin.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testGetDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("operational"))
            .andExpect(jsonPath("$.service").value("dms-admin-service"));
    }
}

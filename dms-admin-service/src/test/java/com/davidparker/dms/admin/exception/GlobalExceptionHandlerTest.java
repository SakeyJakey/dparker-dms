package com.davidparker.dms.admin.exception;

import com.davidparker.dms.admin.dto.UserCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GlobalExceptionHandlerTest.TestController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @RestController
    @RequestMapping("/test")
    static class TestController {
        @PostMapping("/runtime")
        public void throwRuntimeException() {
            throw new RuntimeException("Test runtime exception");
        }

        @PostMapping("/validation")
        public void validateRequest(@RequestBody UserCreateRequest request) {
            // Validation will be handled by @Valid
        }
    }

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testRuntimeExceptionHandling() throws Exception {
        mockMvc.perform(post("/test/runtime")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @WithMockUser(roles = "DMS.Admin")
    void testValidationExceptionHandling() throws Exception {
        UserCreateRequest invalidRequest = new UserCreateRequest();
        // Missing required fields

        mockMvc.perform(post("/test/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").exists());
    }
}

package com.davidparker.dms.admin.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleRuntimeException() {
        RuntimeException ex = new RuntimeException("Test runtime exception");
        ResponseEntity<Map<String, Object>> response = handler.handleRuntimeException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test runtime exception", response.getBody().get("error"));
        assertEquals(400, response.getBody().get("status"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleValidationExceptions() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "object");
        bindingResult.addError(new FieldError("object", "username", "Username is required"));
        bindingResult.addError(new FieldError("object", "email", "Email must be valid"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, String> errors = (Map<String, String>) response.getBody().get("errors");
        assertEquals("Username is required", errors.get("username"));
        assertEquals("Email must be valid", errors.get("email"));
    }

    @Test
    void testHandleGenericException() {
        Exception ex = new Exception("Unexpected error");
        ResponseEntity<Map<String, Object>> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred", response.getBody().get("error"));
        assertEquals(500, response.getBody().get("status"));
    }
}

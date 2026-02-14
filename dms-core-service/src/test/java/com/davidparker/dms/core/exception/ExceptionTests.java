package com.davidparker.dms.core.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionTests {

    @Test
    void testDmsException() {
        DmsException ex = new DmsException("test error");
        assertEquals("test error", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void testDmsExceptionWithCause() {
        Exception cause = new RuntimeException("root cause");
        DmsException ex = new DmsException("test error", cause);
        assertEquals("test error", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void testResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Document not found");
        assertEquals("Document not found", ex.getMessage());
        assertInstanceOf(DmsException.class, ex);
    }

    @Test
    void testResourceNotFoundExceptionWithTypeAndId() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Document", "12345");
        assertEquals("Document with id 12345 not found", ex.getMessage());
    }

    @Test
    void testUnauthorizedException() {
        UnauthorizedException ex = new UnauthorizedException("Access denied");
        assertEquals("Access denied", ex.getMessage());
        assertInstanceOf(DmsException.class, ex);
    }

    @Test
    void testUnauthorizedExceptionWithActionAndResource() {
        UnauthorizedException ex = new UnauthorizedException("delete", "Document");
        assertEquals("Unauthorized to delete Document", ex.getMessage());
    }
}

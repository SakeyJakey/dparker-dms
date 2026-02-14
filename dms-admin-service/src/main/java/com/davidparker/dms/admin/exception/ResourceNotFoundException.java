package com.davidparker.dms.admin.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(String.format("%s with id %s not found", resourceType, resourceId));
    }
}

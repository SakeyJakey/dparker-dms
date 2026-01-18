package com.davidparker.dms.core.exception;

/**
 * Exception thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends DmsException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(String.format("%s with id %s not found", resourceType, resourceId));
    }
}

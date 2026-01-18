package com.davidparker.dms.core.exception;

/**
 * Exception thrown when a user is not authorized to perform an action.
 */
public class UnauthorizedException extends DmsException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String action, String resource) {
        super(String.format("Unauthorized to %s %s", action, resource));
    }
}

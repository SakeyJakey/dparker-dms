package com.davidparker.dms.core.exception;

/**
 * Base exception class for all DMS exceptions.
 */
public class DmsException extends RuntimeException {

    public DmsException(String message) {
        super(message);
    }

    public DmsException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.adarsh.airesumeanalyzer.exception;

/**
 * Custom runtime exception thrown when a requested resource (e.g., a Resume)
 * is not found or is inaccessible to the requesting user.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}

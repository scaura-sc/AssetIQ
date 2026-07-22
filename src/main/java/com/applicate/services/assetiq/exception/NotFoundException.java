package com.applicate.services.assetiq.exception;

/** The primary resource addressed by a request (e.g. path-parameter id) doesn't exist. Maps to HTTP 404. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}

package com.applicate.services.assetiq.exception;

/**
 * The request is well-formed and references valid entities, but the current
 * state of those entities doesn't allow the requested transition (e.g.
 * deploying an asset that isn't STOCK+WORKING, closing a work order without
 * photos, an asset already having an active association). Maps to HTTP 409.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}

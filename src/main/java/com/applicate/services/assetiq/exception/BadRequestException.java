package com.applicate.services.assetiq.exception;

/**
 * A soft-reference in the request payload doesn't resolve (e.g. an unknown
 * category_code), or a structural/field-shape invariant is violated (e.g. a
 * TYPE row whose parent_code doesn't point at a CATEGORY). Maps to HTTP 400.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}

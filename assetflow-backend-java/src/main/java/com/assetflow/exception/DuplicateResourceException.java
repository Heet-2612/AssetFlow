package com.assetflow.exception;

/** Thrown when uniqueness constraints (email, serial number, asset tag, department name, etc.) are violated. */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}

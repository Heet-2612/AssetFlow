package com.assetflow.exception;

/** Thrown when a requested entity (asset, user, department, etc.) does not exist. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String entityName, Long id) {
        return new ResourceNotFoundException(entityName + " not found with id: " + id);
    }
}

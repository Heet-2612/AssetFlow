package com.assetflow.exception;

/** Thrown when an entity is asked to move to a status that isn't reachable from its current status. */
public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}

package com.assetflow.exception;

/** Thrown when a user attempts an action their role does not permit (e.g. non-Asset-Manager approving maintenance). */
public class UnauthorizedActionException extends RuntimeException {

    public UnauthorizedActionException(String message) {
        super(message);
    }
}

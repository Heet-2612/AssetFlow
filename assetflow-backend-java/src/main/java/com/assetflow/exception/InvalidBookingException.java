package com.assetflow.exception;

/** Thrown for structurally invalid booking requests, e.g. end time not after start time. */
public class InvalidBookingException extends RuntimeException {

    public InvalidBookingException(String message) {
        super(message);
    }
}

package com.assetflow.exception;

/** Thrown when a requested booking time slot overlaps an existing booking for the same asset. */
public class BookingConflictException extends RuntimeException {

    public BookingConflictException(String message) {
        super(message);
    }
}

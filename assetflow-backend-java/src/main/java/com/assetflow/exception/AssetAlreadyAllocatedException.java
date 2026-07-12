package com.assetflow.exception;

/**
 * Thrown when attempting to allocate an asset that is already held by someone.
 * Carries the current holder's name so the caller can surface
 * "currently held by X" plus offer a Transfer Request instead.
 */
public class AssetAlreadyAllocatedException extends RuntimeException {

    private final String currentHolderName;
    private final Long currentAllocationId;

    public AssetAlreadyAllocatedException(String message, String currentHolderName, Long currentAllocationId) {
        super(message);
        this.currentHolderName = currentHolderName;
        this.currentAllocationId = currentAllocationId;
    }

    public String getCurrentHolderName() {
        return currentHolderName;
    }

    public Long getCurrentAllocationId() {
        return currentAllocationId;
    }
}

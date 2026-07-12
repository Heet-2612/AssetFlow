package com.assetflow.util;

/**
 * Generates sequential, human-readable asset tags in the form AF-0001, AF-0002, ...
 * The actual "next number" bookkeeping lives in AssetService (backed by the DB via
 * AssetRepository.count() or a dedicated sequence) - this class is a pure formatter
 * so it stays trivially unit-testable and has no persistence concerns.
 */
public final class AssetTagGenerator {

    private static final String PREFIX = "AF-";
    private static final int PAD_LENGTH = 4;

    private AssetTagGenerator() {
    }

    /**
     * @param sequenceNumber a 1-based, monotonically increasing number (e.g. current asset count + 1)
     * @return a formatted tag such as "AF-0001"
     */
    public static String generate(long sequenceNumber) {
        if (sequenceNumber < 1) {
            throw new IllegalArgumentException("sequenceNumber must be >= 1");
        }
        return PREFIX + String.format("%0" + PAD_LENGTH + "d", sequenceNumber);
    }
}

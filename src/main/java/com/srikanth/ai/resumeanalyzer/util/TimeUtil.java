package com.srikanth.ai.resumeanalyzer.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Utility class for time-related operations
 */
public class TimeUtil {

    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    private TimeUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Converts LocalDateTime to milliseconds since epoch
     */
    public static long toMillis(LocalDateTime dateTime) {
        return dateTime.atZone(DEFAULT_ZONE).toInstant().toEpochMilli();
    }

    /**
     * Converts milliseconds since epoch to LocalDateTime
     */
    public static LocalDateTime fromMillis(long millis) {
        return LocalDateTime.ofInstant(
            Instant.ofEpochMilli(millis),
            DEFAULT_ZONE
        );
    }

    /**
     * Gets current time in milliseconds
     */
    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}


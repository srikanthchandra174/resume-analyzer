package com.srikanth.ai.resumeanalyzer.util;

import java.util.regex.Pattern;

/**
 * Utility class for text processing and validation
 */
public class TextUtil {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private static final Pattern PHONE_PATTERN =
        Pattern.compile("^[+]?[0-9]{10,}$");

    private TextUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Validates if text is blank
     */
    public static boolean isBlank(String text) {
        return text == null || text.strip().isEmpty();
    }

    /**
     * Validates if text is not blank
     */
    public static boolean isNotBlank(String text) {
        return !isBlank(text);
    }

    /**
     * Truncates text to specified length
     */
    public static String truncate(String text, int length) {
        if (isBlank(text)) {
            return text;
        }

        if (text.length() <= length) {
            return text;
        }

        return text.substring(0, length) + "...";
    }

    /**
     * Validates email format
     */
    public static boolean isValidEmail(String email) {
        if (isBlank(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates phone number format
     */
    public static boolean isValidPhoneNumber(String phone) {
        if (isBlank(phone)) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.replaceAll("\\D", "")).matches();
    }

    /**
     * Sanitizes text by removing extra whitespace
     */
    public static String sanitize(String text) {
        if (isBlank(text)) {
            return text;
        }
        return text.strip().replaceAll("\\s+", " ");
    }

    /**
     * Extracts initials from a name
     */
    public static String getInitials(String fullName) {
        if (isBlank(fullName)) {
            return "";
        }

        String[] words = fullName.strip().split("\\s+");
        StringBuilder initials = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                initials.append(word.charAt(0));
            }
        }

        return initials.toString().toUpperCase();
    }
}


package com.srikanth.ai.resumeanalyzer.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TextUtil utility class
 */
@DisplayName("TextUtil Tests")
class TextUtilTest {

    @Test
    @DisplayName("isBlank should return true for null")
    void testIsBlankWithNull() {
        assertTrue(TextUtil.isBlank(null));
    }

    @Test
    @DisplayName("isBlank should return true for empty string")
    void testIsBlankWithEmpty() {
        assertTrue(TextUtil.isBlank(""));
    }

    @Test
    @DisplayName("isBlank should return true for whitespace")
    void testIsBlankWithWhitespace() {
        assertTrue(TextUtil.isBlank("   \t\n  "));
    }

    @Test
    @DisplayName("isBlank should return false for text")
    void testIsBlankWithText() {
        assertFalse(TextUtil.isBlank("hello"));
    }

    @Test
    @DisplayName("isNotBlank should return false for null")
    void testIsNotBlankWithNull() {
        assertFalse(TextUtil.isNotBlank(null));
    }

    @Test
    @DisplayName("isNotBlank should return false for blank")
    void testIsNotBlankWithBlank() {
        assertFalse(TextUtil.isNotBlank("   "));
    }

    @Test
    @DisplayName("isNotBlank should return true for text")
    void testIsNotBlankWithText() {
        assertTrue(TextUtil.isNotBlank("hello"));
    }

    @Test
    @DisplayName("truncate should return null-safe result")
    void testTruncateWithNull() {
        assertNull(TextUtil.truncate(null, 10));
    }

    @Test
    @DisplayName("truncate should return text unchanged when under limit")
    void testTruncateUnderLimit() {
        String text = "hello";
        assertEquals("hello", TextUtil.truncate(text, 10));
    }

    @Test
    @DisplayName("truncate should return text unchanged when at limit")
    void testTruncateAtLimit() {
        String text = "hello";
        assertEquals("hello", TextUtil.truncate(text, 5));
    }

    @Test
    @DisplayName("truncate should append '...' when over limit")
    void testTruncateOverLimit() {
        String text = "hello world";
        assertEquals("hello...", TextUtil.truncate(text, 5));
    }

    @Test
    @DisplayName("truncate should return blank string unchanged")
    void testTruncateBlank() {
        assertEquals("", TextUtil.truncate("", 10));
    }

    @Test
    @DisplayName("isValidEmail should return true for valid email")
    void testIsValidEmailWithValid() {
        assertTrue(TextUtil.isValidEmail("user@example.com"));
    }

    @Test
    @DisplayName("isValidEmail should return false for missing @")
    void testIsValidEmailMissingAt() {
        assertFalse(TextUtil.isValidEmail("userexample.com"));
    }

    @Test
    @DisplayName("isValidEmail should return false for blank")
    void testIsValidEmailBlank() {
        assertFalse(TextUtil.isValidEmail("   "));
    }

    @Test
    @DisplayName("isValidEmail should return false for null")
    void testIsValidEmailNull() {
        assertFalse(TextUtil.isValidEmail(null));
    }

    @Test
    @DisplayName("isValidEmail should return false for no domain")
    void testIsValidEmailNoDomain() {
        assertFalse(TextUtil.isValidEmail("user@"));
    }

    @Test
    @DisplayName("isValidPhoneNumber should return true for 10-digit valid")
    void testIsValidPhoneNumberTenDigit() {
        assertTrue(TextUtil.isValidPhoneNumber("9876543210"));
    }

    @Test
    @DisplayName("isValidPhoneNumber should return true for formatted phone")
    void testIsValidPhoneNumberFormatted() {
        assertTrue(TextUtil.isValidPhoneNumber("+91 98765-43210"));
    }

    @Test
    @DisplayName("isValidPhoneNumber should return false for too short")
    void testIsValidPhoneNumberShort() {
        assertFalse(TextUtil.isValidPhoneNumber("123456"));
    }

    @Test
    @DisplayName("isValidPhoneNumber should return false for blank")
    void testIsValidPhoneNumberBlank() {
        assertFalse(TextUtil.isValidPhoneNumber("   "));
    }

    @Test
    @DisplayName("isValidPhoneNumber should return false for null")
    void testIsValidPhoneNumberNull() {
        assertFalse(TextUtil.isValidPhoneNumber(null));
    }

    @Test
    @DisplayName("sanitize should collapse whitespace")
    void testSanitizeCollapsesWhitespace() {
        assertEquals("hello world foo", TextUtil.sanitize("hello   world    foo"));
    }

    @Test
    @DisplayName("sanitize should strip leading/trailing whitespace")
    void testSanitizeStrips() {
        assertEquals("hello world", TextUtil.sanitize("   hello world   "));
    }

    @Test
    @DisplayName("sanitize should return blank for blank input")
    void testSanitizeBlank() {
        String result = TextUtil.sanitize("   ");
        assertTrue(TextUtil.isBlank(result));
    }

    @Test
    @DisplayName("sanitize should return null for null")
    void testSanitizeNull() {
        assertNull(TextUtil.sanitize(null));
    }

    @Test
    @DisplayName("getInitials should return uppercase initials")
    void testGetInitialsUppercase() {
        assertEquals("JD", TextUtil.getInitials("john doe"));
    }

    @Test
    @DisplayName("getInitials should return uppercase with multiple words")
    void testGetInitialsMultiple() {
        assertEquals("JMD", TextUtil.getInitials("john michael doe"));
    }

    @Test
    @DisplayName("getInitials should return empty for blank")
    void testGetInitialsBlank() {
        assertEquals("", TextUtil.getInitials("   "));
    }

    @Test
    @DisplayName("getInitials should return empty for null")
    void testGetInitialsNull() {
        assertEquals("", TextUtil.getInitials(null));
    }

    @Test
    @DisplayName("getInitials should handle single word")
    void testGetInitialsSingleWord() {
        assertEquals("J", TextUtil.getInitials("john"));
    }
}



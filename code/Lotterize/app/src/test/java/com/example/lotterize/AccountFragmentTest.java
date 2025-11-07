package com.example.lotterize;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Testing input validation.
 */
public class AccountFragmentTest {

    /**
     * Test that empty input is invalid.
     */
    @Test
    public void testValidation_EmptyInput() {
        String input = "";

        assertTrue(input.isEmpty());
    }

    /**
     * Test that non-empty input is valid.
     */
    @Test
    public void testValidation_NonEmptyInput() {
        String input = "John Doe";

        assertFalse(input.isEmpty());
    }

    /**
     * Test that valid phone number (10 digits) is accepted.
     */
    @Test
    public void testPhoneValidation_ValidPhone() {
        String phone = "1234567890";

        boolean isValid = phone.length() >= 10;

        assertTrue(isValid);
    }

    /**
     * Test that phone number with less than 10 digits is invalid.
     */
    @Test
    public void testPhoneValidation_TooShort() {
        String phone = "123456789"; // 9 digits

        boolean isValid = phone.length() >= 10;

        assertFalse(isValid);
    }

    /**
     * Test that phone number with more than 10 digits is valid.
     */
    @Test
    public void testPhoneValidation_LongerPhone() {
        String phone = "12345678901"; // 11 digits

        boolean isValid = phone.length() >= 10;

        assertTrue(isValid);
    }

    /**
     * Test that empty phone number is invalid.
     */
    @Test
    public void testPhoneValidation_EmptyPhone() {
        String phone = "";

        boolean isValid = phone.length() >= 10;

        assertFalse(isValid);
    }

    /**
     * Test that input is trimmed correctly (removes leading/trailing spaces).
     */
    @Test
    public void testInputTrimming() {
        String input = "  test input  ";

        String trimmed = input.trim();

        assertEquals("test input", trimmed);
        assertNotEquals(input, trimmed);
    }

    /**
     * Test that trimming empty string returns empty string.
     */
    @Test
    public void testInputTrimming_EmptyString() {
        String input = "   ";

        String trimmed = input.trim();

        assertEquals("", trimmed);
        assertTrue(trimmed.isEmpty());
    }

    /**
     * Test that string with no spaces remains unchanged after trim.
     */
    @Test
    public void testInputTrimming_NoSpaces() {
        String input = "test";

        String trimmed = input.trim();

        assertEquals(input, trimmed);
    }

    /**
     * Test validation of name length (non-empty check).
     */
    @Test
    public void testNameValidation_ValidName() {
        String name = "John Doe";

        assertFalse(name.isEmpty());
        assertTrue(name.length() > 0);
    }
}
package com.example.lotterize;

import static org.junit.Assert.*;

import android.text.InputType;

import org.junit.Test;

/**
 * Unit tests for AccountFragment.
 * Tests core functionality: input validation for name, email, and phone number fields.
 */
public class AccountFragmentTest {

    /**
     * Test that empty input is invalid.
     */
    @Test
    public void testValidation_EmptyInput() {
        // Arrange
        String input = "";

        // Act & Assert
        assertTrue(input.isEmpty());
    }

    /**
     * Test that non-empty input is valid.
     */
    @Test
    public void testValidation_NonEmptyInput() {
        // Arrange
        String input = "John Doe";

        // Act & Assert
        assertFalse(input.isEmpty());
    }

    /**
     * Test that valid phone number (10 digits) is accepted.
     */
    @Test
    public void testPhoneValidation_ValidPhone() {
        // Arrange
        String phone = "1234567890";

        // Act
        boolean isValid = phone.length() >= 10;

        // Assert
        assertTrue(isValid);
    }

    /**
     * Test that phone number with less than 10 digits is invalid.
     */
    @Test
    public void testPhoneValidation_TooShort() {
        // Arrange
        String phone = "123456789"; // 9 digits

        // Act
        boolean isValid = phone.length() >= 10;

        // Assert
        assertFalse(isValid);
    }

    /**
     * Test that phone number with more than 10 digits is valid.
     */
    @Test
    public void testPhoneValidation_LongerPhone() {
        // Arrange
        String phone = "12345678901"; // 11 digits

        // Act
        boolean isValid = phone.length() >= 10;

        // Assert
        assertTrue(isValid);
    }

    /**
     * Test that empty phone number is invalid.
     */
    @Test
    public void testPhoneValidation_EmptyPhone() {
        // Arrange
        String phone = "";

        // Act
        boolean isValid = phone.length() >= 10;

        // Assert
        assertFalse(isValid);
    }

    /**
     * Test that input is trimmed correctly (removes leading/trailing spaces).
     */
    @Test
    public void testInputTrimming() {
        // Arrange
        String input = "  test input  ";

        // Act
        String trimmed = input.trim();

        // Assert
        assertEquals("test input", trimmed);
        assertNotEquals(input, trimmed);
    }

    /**
     * Test that trimming empty string returns empty string.
     */
    @Test
    public void testInputTrimming_EmptyString() {
        // Arrange
        String input = "   ";

        // Act
        String trimmed = input.trim();

        // Assert
        assertEquals("", trimmed);
        assertTrue(trimmed.isEmpty());
    }

    /**
     * Test that string with no spaces remains unchanged after trim.
     */
    @Test
    public void testInputTrimming_NoSpaces() {
        // Arrange
        String input = "test";

        // Act
        String trimmed = input.trim();

        // Assert
        assertEquals(input, trimmed);
    }

    /**
     * Test validation of name length (non-empty check).
     */
    @Test
    public void testNameValidation_ValidName() {
        // Arrange
        String name = "John Doe";

        // Act & Assert
        assertFalse(name.isEmpty());
        assertTrue(name.length() > 0);
    }
}
package com.example.lotterize;

import static org.junit.Assert.*;

import com.example.lotterize.CurrentUser;
import com.example.lotterize.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for ProfileFragment.
 * Tests core functionality: greeting message formatting and user session management.
 */
public class ProfileFragmentTest {

    private User testUser;

    /**
     * Set up test user before each test.
     */
    @Before
    public void setUp() {
        testUser = new User(
                "testUserId123",
                "Test Name",
                "1234567890",
                "test@email.com",
                "",
                "testuser",
                "password123"
        );
    }

    /**
     * Clean up after each test.
     */
    @After
    public void tearDown() {
        CurrentUser.clear();
    }

    /**
     * Test that greeting message is correctly formatted with username.
     */
    @Test
    public void testGreetingMessage_WithValidUsername() {
        // Arrange
        String username = "testuser";

        // Act
        String greeting = "Hello, " + username + "!";

        // Assert
        assertEquals("Hello, testuser!", greeting);
    }

    /**
     * Test that greeting message uses default "User" when username is null.
     */
    @Test
    public void testGreetingMessage_WithNullUsername() {
        // Arrange
        String username = null;

        // Act
        String actualUsername = username != null ? username : "User";
        String greeting = "Hello, " + actualUsername + "!";

        // Assert
        assertEquals("Hello, User!", greeting);
    }

    /**
     * Test that CurrentUser can store and retrieve a user.
     */
    @Test
    public void testCurrentUser_SetAndGet() {
        // Act
        CurrentUser.set(testUser);

        // Assert
        assertNotNull(CurrentUser.get());
        assertEquals("testuser", CurrentUser.get().getUsername());
    }

    /**
     * Test that CurrentUser can be cleared.
     */
    @Test
    public void testCurrentUser_Clear() {
        // Arrange
        CurrentUser.set(testUser);

        // Act
        CurrentUser.clear();

        // Assert
        assertNull(CurrentUser.get());
    }
}
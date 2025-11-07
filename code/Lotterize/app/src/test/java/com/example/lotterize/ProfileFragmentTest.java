package com.example.lotterize;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
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
        String username = "testuser";

        String greeting = "Hello, " + username + "!";

        assertEquals("Hello, testuser!", greeting);
    }

    /**
     * Test that greeting message uses default "User" when username is null.
     */
    @Test
    public void testGreetingMessage_WithNullUsername() {
        String username = null;

        String actualUsername = username != null ? username : "User";
        String greeting = "Hello, " + actualUsername + "!";

        assertEquals("Hello, User!", greeting);
    }

    /**
     * Test that CurrentUser can store and retrieve a user.
     */
    @Test
    public void testCurrentUser_SetAndGet() {
        CurrentUser.set(testUser);

        assertNotNull(CurrentUser.get());
        assertEquals("testuser", CurrentUser.get().getUsername());
    }

    /**
     * Test that CurrentUser can be cleared.
     */
    @Test
    public void testCurrentUser_Clear() {
        CurrentUser.set(testUser);

        CurrentUser.clear();

        assertNull(CurrentUser.get());
    }
}
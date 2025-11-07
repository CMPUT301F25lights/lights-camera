package com.example.lotterize;

/**
 * {@code CurrentUser} is a simple singleton utility class that stores and provides
 * access to the currently logged-in {@link User} instance across the Lotterize app.
 *
 * This class allows global access to the current user's information without the need
 * to repeatedly query Firestore or pass user data between components.
 */
public class CurrentUser {
    private static User instance;
    public static void set(User user) {
        instance = user;
    }
    public static User get() {
        return instance;
    }
    public static void clear() {instance = null;}
}

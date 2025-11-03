package com.example.lotterize;

import java.util.ArrayList;

/**
 * Models a User. Contains all attributes of a user including
 * profile info and event participation details.
 *
 * Firestore requires:
 *  - public no-arg constructor
 *  - public getters/setters for all stored fields
 */
public class User {

    private String name;
    private String userId;
    private String phoneNumber;
    private String email;
    private String coordinates;
    private String username;
    private String password;
    private ArrayList<String> registeredEventIds;
    private ArrayList<String> ownedEventIds;

    /**
     * Full constructor for creating a complete User in memory.
     */
    public User(String userId, String name, String phoneNumber, String email,
                String coordinates, String username, String password) {
        this.userId = userId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.coordinates = coordinates;
        this.username = username;
        this.password = password;
        this.registeredEventIds = new ArrayList<>();
        this.ownedEventIds = new ArrayList<>();
    }

    /**
     * Required no-argument constructor for Firestore deserialization.
     */
    public User() {
        this.registeredEventIds = new ArrayList<>();
        this.ownedEventIds = new ArrayList<>();
    }

    /**
     * Alternative minimal constructor for login tests.
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.registeredEventIds = new ArrayList<>();
        this.ownedEventIds = new ArrayList<>();
    }

    /** @return name of user */
    public String getName() { return name; }
    /** @param name updated user name */
    public void setName(String name) { this.name = name; }

    /** @return phone number of user */
    public String getPhoneNumber() { return phoneNumber; }
    /** @param phoneNumber updated phone number */
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    /** @return email of user */
    public String getEmail() { return email; }
    /** @param email updated email address */
    public void setEmail(String email) { this.email = email; }

    /** @return coordinates of user (location reference) */
    public String getCoordinates() { return coordinates; }
    /** @param coordinates updated GPS/location reference */
    public void setCoordinates(String coordinates) { this.coordinates = coordinates; }

    /** @return unique Firestore user ID */
    public String getUserId() { return userId; }
    /** @param userId new Firestore user ID */
    public void setUserId(String userId) { this.userId = userId; }

    /** @return username */
    public String getUsername() { return username; }
    /** @param username updated login username */
    public void setUsername(String username) { this.username = username; }

    /** @return password */
    public String getPassword() { return password; }
    /** @param password updated login password */
    public void setPassword(String password) { this.password = password; }

    /** @return list of event IDs user has registered for */
    public ArrayList<String> getRegisteredEventIds() { return registeredEventIds; }
    /** @param registeredEventIds full updated list from Firestore */
    public void setRegisteredEventIds(ArrayList<String> registeredEventIds) {
        this.registeredEventIds = registeredEventIds;
    }

    /** @return list of event IDs user owns */
    public ArrayList<String> getOwnedEventIds() { return ownedEventIds; }
    /** @param ownedEventIds full updated list from Firestore */
    public void setOwnedEventIds(ArrayList<String> ownedEventIds) {
        this.ownedEventIds = ownedEventIds;
    }

    /**
     * Adds an event ID to the list of events that this user owns.
     * Automatically creates list if missing (first-time user).
     *
     * @param eventId Firestore event document ID
     */
    public void addOwnedEvent(String eventId) {
        if (this.ownedEventIds == null) this.ownedEventIds = new ArrayList<>();
        this.ownedEventIds.add(eventId);
    }

    /**
     * Adds an event ID to list of events user registered for.
     * Automatically creates list if missing.
     *
     * @param eventId Firestore event document ID
     */
    public void addRegisteredEvent(String eventId) {
        if (this.registeredEventIds == null) this.registeredEventIds = new ArrayList<>();
        this.registeredEventIds.add(eventId);
    }

}

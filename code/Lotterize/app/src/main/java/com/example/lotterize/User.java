package com.example.lotterize;

import java.util.ArrayList;

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

    public User(String userId, String name, String phoneNumber, String email,
                String coordinates, String username, String password) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.coordinates = coordinates;
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.registeredEventIds = new ArrayList<>();
        this.ownedEventIds = new ArrayList<>();
    }
    public User() {}
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.registeredEventIds = new ArrayList<>();
        this.ownedEventIds = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public void addRegisteredEvent(String eventId) {
        this.registeredEventIds.add(eventId);
    }
    public void addOwnedEvent(String eventId) {
        this.ownedEventIds.add(eventId);
    }
}

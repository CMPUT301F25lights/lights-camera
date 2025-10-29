package com.example.lotterize;

public class User {

    private String name;
    private long userId;
    private String phoneNumber;
    private String email;
    private String coordinates;

    public User(long userId, String name, String phoneNumber, String email, String coordinates) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.coordinates = coordinates;
        this.userId = userId;
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

    public long getUserId() {
        return userId;
    }
}

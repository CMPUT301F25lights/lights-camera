package com.example.lotterize;

public class User {

    private String name;
    private long id;
    private String phoneNumber;
    private String email;
    private String coordinates;

    public User(String name, String phoneNumber, String email, String coordinates) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.coordinates = coordinates;
        this.id = 0; // implement later
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

    public long getId() {
        return id;
    }
}

package com.example.lotterize;

import java.util.ArrayList;

public class User {

    private long id;
    private Boolean receiveNotifications;
    private String name;
    private String phoneNumber;
    private String locationCoordinates;
    private String email;
    private ArrayList<Long> deviceId;
    private ArrayList<Long> eventIdRegistered;
    private ArrayList<Long> eventIdOwned;

    public User(long id, Boolean receiveNotifications, String name, String phoneNumber, String locationCoordinates, String email,
                ArrayList<Long> deviceId, ArrayList<Long> eventIdRegistered, ArrayList<Long> eventIdOwned) {
        this.id = id;
        this.receiveNotifications = receiveNotifications;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.locationCoordinates = locationCoordinates;
        this.email = email;
        this.deviceId = deviceId;
        this.eventIdRegistered = eventIdRegistered;
        this.eventIdOwned = eventIdOwned;
    }
}

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
    private boolean wantNotification;

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
        wantNotification = true;
    }

    public long getId() {
        return id;
    }

    public Boolean getReceiveNotifications() {
        return receiveNotifications;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getLocationCoordinates() {
        return locationCoordinates;
    }

    public String getEmail(){
        return email;
    }

    public ArrayList<Long> getDeviceId() {
        return deviceId;
    }

    public ArrayList<Long> getEventIdRegistered() {
        return eventIdRegistered;
    }
    public ArrayList<Long> getEventIdOwned() {
        return eventIdOwned;
    }
    public void turnOnNotification(){
        wantNotification = true;
    }

    public void turnOffNotification() {
        wantNotification = false;
    }
}


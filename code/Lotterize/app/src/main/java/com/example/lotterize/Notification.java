package com.example.lotterize;

import java.util.ArrayList;

public class Notification {

    private long notificationId;
    private User sender;
    private ArrayList<User> receivers;
    private String message;

    public Notification(long notificationId, User sender, ArrayList<User> receivers, String message){
        this.notificationId = notificationId;
        this.sender = sender;
        this.receivers = receivers;
        this.message = message;
    }

    public long getNotificationId() {
        return notificationId;
    }

    public User getSender() {
        return sender;
    }

    public ArrayList<User> getReceivers() {
        return receivers;
    }

    public String getMessage() {
        return message;
    }
}
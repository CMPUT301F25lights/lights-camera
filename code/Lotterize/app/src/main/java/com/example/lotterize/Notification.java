package com.example.lotterize;

import com.google.firebase.Timestamp;

import java.util.ArrayList;

public class Notification {

    private long notificationId;
    private long senderId;
    private String message;
    private Timestamp time;
    private ArrayList<Long> receiversId;

    public Notification(long notificationId, long senderId, String message, Timestamp time, ArrayList<Long> receiversId){
        this.notificationId = notificationId;
        this.senderId = senderId;
        this.message = message;
        this.time = time;
        this.receiversId = receiversId;
    }
}

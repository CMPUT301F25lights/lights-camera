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

    public long getNotificationId() {
        return notificationId;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message){
        this.message = message;
    }

    public ArrayList<Long> getReceiversId() {
        return receiversId;
    }
    public void setReceiversId(ArrayList<Long> receiversId) {
        this.receiversId = receiversId;
    }

    public long getSenderId() {
        return senderId;
    }
    public void setSenderId(long senderId){
        this.senderId = senderId;
    }

    public Timestamp getTime() {
        return time;
    }
    public void setTime(Timestamp time){
        this.time = time;
    }

}

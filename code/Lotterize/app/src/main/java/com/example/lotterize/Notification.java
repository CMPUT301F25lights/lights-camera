package com.example.lotterize;

import com.google.firebase.Timestamp;

import java.util.ArrayList;

public class Notification {

    private String notificationId;

    private String senderName;

    private String senderId;
    private String message;
    private Timestamp time;
    private ArrayList<String> receiversId;
    public Notification(){
        notificationId = null;
        senderName = null;
        senderId = null;
        message = null;
        time = Timestamp.now();
        receiversId = new ArrayList<String>();
    }
    public Notification(String notificationId, String senderId, String senderName, String message, Timestamp time, ArrayList<String> receiversId){
        this.notificationId = notificationId;
        this.senderName = senderName;
        this.senderId = senderId;
        this.message = message;
        this.time = time;
        this.receiversId = receiversId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message){
        this.message = message;
    }

    public ArrayList<String> getReceiversId() {
        return receiversId;
    }
    public void setReceiversId(ArrayList<String> receiversId) {
        this.receiversId = receiversId;
    }


    public void setNotificationId(String notificationId){
        this.notificationId = notificationId;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time){
        this.time = time;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

}

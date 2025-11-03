package com.example.lotterize;

import com.google.firebase.Timestamp;

import java.util.ArrayList;

/**
 * Model class for Notifications. Contains all data related to notifications.
 */
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

    /**
     * @return String - name of sender
     */
    public String getSenderName() {
        return senderName;
    }

    /**
     * @param senderName - name of sender
     */
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    /**
     * @return String - id of the notification
     */
    public String getNotificationId() {
        return notificationId;
    }

    /**
     * @return String - the message/contents of the notification
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message - the contents/message of the notification
     */
    public void setMessage(String message){
        this.message = message;
    }

    /**
     * @return ArrayList<String> - userIds of all the users receiving the notification
     */
    public ArrayList<String> getReceiversId() {
        return receiversId;
    }

    /**
     * @param receiversId - list of ids of all the receivers
     */
    public void setReceiversId(ArrayList<String> receiversId) {
        this.receiversId = receiversId;
    }

    /**
     * @param notificationId - id of the notification
     */
    public void setNotificationId(String notificationId){
        this.notificationId = notificationId;
    }

    /**
     * @return Timestamp - the time of the notification
     */
    public Timestamp getTime() {
        return time;
    }

    /**
     * @param time - time of the notification
     */
    public void setTime(Timestamp time){
        this.time = time;
    }

    /**
     * @return String - id of the sender
     */
    public String getSenderId() {
        return senderId;
    }

    /**
     * @param senderId - id of sender
     */
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

}

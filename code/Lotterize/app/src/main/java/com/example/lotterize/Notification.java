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

    /**
     * This is the no-argument constructor when creating a new Notification instance
     * with default values. The {@code time} is initialized to the current time and
     * {@code receiversId} is initialized as an empty list.
     */
    public Notification(){
        notificationId = null;
        senderName = null;
        senderId = null;
        message = null;
        time = Timestamp.now();
        receiversId = new ArrayList<String>();
    }

    /**
     * This is the constructor when creating a new Notification instance with all fields specified.
     *
     * @param notificationId
     *      The id of this notification
     * @param senderId
     *      The id of the user who sent this notification
     * @param senderName
     *      The display name of the sender
     * @param message
     *      The contents of the notification
     * @param time
     *      The timestamp when the notification was created
     * @param receiversId
     *      The list of user ids who will receive this notification
     */
    public Notification(String notificationId, String senderId, String senderName, String message, Timestamp time, ArrayList<String> receiversId){
        this.notificationId = notificationId;
        this.senderName = senderName;
        this.senderId = senderId;
        this.message = message;
        this.time = time;
        this.receiversId = receiversId;
    }

    /**
     * This returns the sender's display name.
     * @return
     *      Returns a string representing the sender's name
     */
    public String getSenderName() {
        return senderName;
    }

    /**
     * This updates the sender's display name.
     * @param senderName
     *      The name of the sender
     */
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    /**
     * This returns the notification id.
     * @return
     *      Returns a string representing the id of the notification
     */
    public String getNotificationId() {
        return notificationId;
    }

    /**
     * This returns the notification message.
     * @return
     *      Returns a string containing the contents of the notification
     */
    public String getMessage() {
        return message;
    }

    /**
     * This updates the notification message.
     * @param message
     *      The new contents of the notification
     */
    public void setMessage(String message){
        this.message = message;
    }

    /**
     * This returns the list of receiver user ids.
     * @return
     *      Returns an {@code ArrayList<String>} of user ids who receive this notification
     */
    public ArrayList<String> getReceiversId() {
        return receiversId;
    }

    /**
     * This updates the list of receiver user ids.
     * @param receiversId
     *      The list of ids of all receivers
     */
    public void setReceiversId(ArrayList<String> receiversId) {
        this.receiversId = receiversId;
    }

    /**
     * This updates the notification id.
     * @param notificationId
     *      The id of the notification
     */
    public void setNotificationId(String notificationId){
        this.notificationId = notificationId;
    }

    /**
     * This returns the timestamp of the notification.
     * @return
     *      Returns a {@code Timestamp} representing when the notification was created
     */
    public Timestamp getTime() {
        return time;
    }

    /**
     * This updates the timestamp of the notification.
     * @param time
     *      The time of the notification
     */
    public void setTime(Timestamp time){
        this.time = time;
    }

    /**
     * This returns the sender's user id.
     * @return
     *      Returns a string representing the id of the sender
     */
    public String getSenderId() {
        return senderId;
    }

    /**
     * This updates the sender's user id.
     * @param senderId
     *      The id of the sender
     */
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

}

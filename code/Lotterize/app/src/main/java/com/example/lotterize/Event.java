package com.example.lotterize;


import com.google.firebase.Timestamp;
import java.util.ArrayList;

/**
 * Model of Events. Stores all of the information related to events.
 */
public class Event {

    private String eventId;
    private String ownerId;
    private ArrayList<String> waitList;
    private ArrayList<String> selectedList;
    private ArrayList<String> cancelledList;
    private ArrayList<String> finalList;
    private String eventName;
    private Timestamp date;
    private Timestamp registrationStart;
    private Timestamp registrationDeadline;
    private String location;
    private Long totalSpots;
    private String description;
    private Long entrantsLimit;
    private String qrCode;

    // poster image


    public Event(String eventId, String ownerId, ArrayList<String> waitList, ArrayList<String> selectedList, ArrayList<String>cancelledList,
                 ArrayList<String> finalList, String eventName, Timestamp date, Timestamp registrationStart, Timestamp registrationDeadline, String location,
                 long totalSpots, String description, long entrantsLimit, String qrCode){
        this.eventId = eventId;
        this.ownerId = ownerId;
        this.waitList = waitList;
        this.selectedList = selectedList;
        this.cancelledList = cancelledList;
        this.finalList = finalList;
        this.eventName = eventName;
        this.date = date;
        this.registrationStart = registrationStart;
        this.registrationDeadline = registrationDeadline;
        this.location = location;
        this.totalSpots = totalSpots;
        this.description = description;
        this.entrantsLimit = entrantsLimit;
        this.qrCode = qrCode;
    }

    /**
     * @return String - id of event
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * @return String - id of event owner
     */
    public String getOwnerId() {
        return ownerId;
    }

    /**
     * @return ArrayList<String>- list containing ids of all users in waiting list
     */
    public ArrayList<String> getWaitList() {
        return waitList;
    }

    /**
     * @return ArrayList<String>- list containing ids of all users in selected list
     */
    public ArrayList<String> getSelectedList() {
        return selectedList;
    }

    /**
     * @return ArrayList<String>- list containing ids of all users in cancelled list
     */
    public ArrayList<String> getCancelledList() {
        return cancelledList;
    }

    /**
     * @return ArrayList<String>- list containing ids of all users in final list
     */
    public ArrayList<String> getFinalList() {
        return finalList;
    }

    /**
     * @return String- name of event
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * @return Timestamp- date of the event
     */
    public Timestamp getDate(){
        return date;
    }

    /**
     * @return Timestamp- registration start time
     */
    public Timestamp getRegistrationStart() {
        return registrationStart;
    }

    /**
     * @return Timestamp- registration dead line
     */
    public Timestamp getRegistrationDeadline() {
        return registrationDeadline;
    }

    /**
     * @return String- location of event
     */
    public String getLocation(){
        return location;
    }

    /**
     * @return long- total available spots
     */
    public long getTotalSpots() {
        return totalSpots;
    }

    /**
     * @return String- event description
     */
    public String getDescription(){
        return description;
    }

    /**
     * @return long- maximum number of entrants
     */
    public long getEntrantsLimit(){
        return entrantsLimit;
    }

    /**
     * @return String- qrCode in string form
     */
    public String getQrCode() {
        return qrCode;
    }
}
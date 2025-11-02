package com.example.lotterize;


import com.google.firebase.Timestamp;
import java.util.ArrayList;

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

    public String getEventId() {
        return eventId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public ArrayList<String> getWaitList() {
        return waitList;
    }

    public ArrayList<String> getSelectedList() {
        return selectedList;
    }

    public ArrayList<String> getCancelledList() {
        return cancelledList;
    }

    public ArrayList<String> getFinalList() {
        return finalList;
    }

    public String getEventName() {
        return eventName;
    }

    public Timestamp getDate(){
        return date;
    }

    public Timestamp getRegistrationStart() {
        return registrationStart;
    }

    public Timestamp getRegistrationDeadline() {
        return registrationDeadline;
    }

    public String getLocation(){
        return location;
    }

    public long getTotalSpots() {
        return totalSpots;
    }

    public String getDescription(){
        return description;
    }

    public long getEntrantsLimit(){
        return entrantsLimit;
    }

    public String getQrCode() {
        return qrCode;
    }
}
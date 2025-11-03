package com.example.lotterize;


import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

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
    public static Event addEventDetailsFromSnapShot(DocumentSnapshot doc){
        String eventId = doc.getString("eventId");
        String ownerId = doc.getString("ownerId");

        @SuppressWarnings("unchecked")
        ArrayList<String> waitList = (ArrayList<String>) (doc.get("waitList") != null ? doc.get("waitList") : new ArrayList<String>());
        @SuppressWarnings("unchecked")
        ArrayList<String> selectedList = (ArrayList<String>) (doc.get("selectedList") != null ? doc.get("selectedList") : new ArrayList<String>());
        @SuppressWarnings("unchecked")
        ArrayList<String> cancelledList = (ArrayList<String>) (doc.get("cancelledList") != null ? doc.get("cancelledList") : new ArrayList<String>());
        @SuppressWarnings("unchecked")
        ArrayList<String> finalList = (ArrayList<String>) (doc.get("finalList") != null ? doc.get("finalList") : new ArrayList<String>());

        String eventName = doc.getString("eventName");
        Timestamp date = doc.getTimestamp("date");
        Timestamp registrationStart = doc.getTimestamp("registrationStart");
        Timestamp registrationDeadline = doc.getTimestamp("registrationDeadline");
        String location = doc.getString("location");
        Long totalSpots = doc.getLong("totalSpots");
        String description = doc.getString("description");
        Long entrantsLimit = doc.getLong("entrantsLimit");
        String qrCode = doc.getString("qrCode");

        return new Event(eventId, ownerId, waitList,  selectedList, cancelledList,
                finalList, eventName, date, registrationStart, registrationDeadline, location,
                totalSpots,  description,  entrantsLimit,  qrCode);

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


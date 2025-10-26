package com.example.lotterize;

import java.util.ArrayList;
import java.util.Calendar;

public class Event {

    private String eventOwner;
    private ArrayList<String> waitList;
    private String eventName;
    private String date;
    private String location;
    private long totalSpots;
    private long waitListLength;
    private String description;
    private long entrantsLimit;
    private long sampleSize;

    // poster image

    // qr code

    public Event(String eventOwner, String eventName, String date, String location,
                 long totalSpots, long waitListLength, String description, long entrantsLimit, long sampleSize,ArrayList<String> waitList){
        this.eventName = eventName;
        this.date = date;
        this. totalSpots = totalSpots;
        this.location = location;
        this.waitListLength = waitListLength;
        this.description = description;
        this.entrantsLimit = entrantsLimit;
        this.sampleSize = sampleSize;
        this.waitList = waitList;
    }

    public String getEventOwner(){
        return eventOwner;
    }
    public ArrayList<String> getWaitList(){
        return waitList;
    }
    public String getEventName(){
        return eventName;
    }

    public String getDate(){
        return date;
    }

    public String getLocation(){
        return location;
    }

    public long getTotalSpots() {
        return totalSpots;
    }

    public long getWaitListLength() {
        return waitListLength;
    }

    public String getDescription() {
        return description;
    }

    public long getEntrantsLimit() {
        return entrantsLimit;
    }

    public long getSampleSize() {
        return sampleSize;
    }
}

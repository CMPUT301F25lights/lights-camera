package com.example.lotterize;

import java.util.ArrayList;
import java.util.Calendar;

public class Event {

    private String eventOwner;
    private ArrayList<String> waitList;
    private String eventName;
    private String date;
    private String location;
    private int totalSpots;
    private int waitListLength;
    private String description;
    private int entrantsLimit;
    private int sampleSize;

    // poster image

    // qr code

    public Event(String eventName, String date, String location,
                 int totalSpots, int waitListLength, String description, int entrantsLimit, int sampleSize,ArrayList<String> waitList){
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

    public int getTotalSpots() {
        return totalSpots;
    }

    public int getWaitListLength() {
        return waitListLength;
    }

    public String getDescription() {
        return description;
    }

    public int getEntrantsLimit() {
        return entrantsLimit;
    }

    public int getSampleSize() {
        return sampleSize;
    }
}

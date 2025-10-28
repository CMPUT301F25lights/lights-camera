package com.example.lotterize;


import com.google.firebase.Timestamp;
import java.util.ArrayList;

public class Event {

    private long eventId;
    private long ownerId;
    private ArrayList<Long> waitList;
    private ArrayList<Long> selectedList;
    private ArrayList<Long> cancelledList;
    private ArrayList<Long> finalList;
    private String eventName;
    private Timestamp date;
    private Timestamp registrationDeadline;
    private String location;
    private long totalSpots;
    private String description;
    private long entrantsLimit;
    private long sampleSize;
    private String qrCode;

    // poster image


    public Event(long eventId, long ownerId, ArrayList<Long> waitList, ArrayList<Long> selectedList, ArrayList<Long>cancelledList,
                 ArrayList<Long> finalList, String eventName, Timestamp date, Timestamp registrationDeadline, String location,
                 long totalSpots, String description, long entrantsLimit, long sampleSize, String qrCode){
        this.eventId = eventId;
        this.ownerId = ownerId;
        this.waitList = waitList;
        this.selectedList = selectedList;
        this.cancelledList = cancelledList;
        this.finalList = finalList;
        this.eventName = eventName;
        this.date = date;
        this.registrationDeadline = registrationDeadline;
        this.location = location;
        this.totalSpots = totalSpots;
        this.description = description;
        this.entrantsLimit = entrantsLimit;
        this.sampleSize = sampleSize;
        this.qrCode = qrCode;
    }
}

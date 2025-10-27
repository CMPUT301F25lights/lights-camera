package com.example.lotterize;

import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;

/**
 * I added a lottery method into Event and changed some code in notification as well to
 * notify people of selection.
 */
public class Event {

    private User owner;
    private ArrayList<User> waitList;
    private ArrayList<User> selectedList;
    private ArrayList<User> cancelledList;
    private ArrayList<User> finalList;
    private long eventName;
    private long date;
    private long registrationDeadline;
    private String location;
    private long totalSpots;
    private String description;
    private long entrantsLimit;
    private long sampleSize;

    // poster image

    // qr code

    public Event(User owner, ArrayList<User> waitList, ArrayList<User> selectedList,ArrayList<User> cancelledList, ArrayList<User> finalList,
                 long eventName, long date, long registrationDeadline, String location, long totalSpots,
                 String description, long entrantsLimit, long sampleSize){
        this.owner = owner;
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
    }

    public void runLottery() {
        if (waitList.size() == 0) return;

        ArrayList<User> pool = new ArrayList<>(waitList);

        while (selectedList.size() < sampleSize && !pool.isEmpty()) {
            int randomIndex = (int)(Math.random() * pool.size());
            User chosen = pool.remove(randomIndex);
            selectedList.add(chosen);
        }

        // notify selected entrants
        Notification.notifySelectedEntrants(selectedList, this);
    }

    public User getOwner(){
        return owner;
    }

    public ArrayList<User> getWaitList(){
        return waitList;
    }

    public ArrayList<User> getSelectedList(){
        return selectedList;
    }

    public ArrayList<User> getCancelledList(){
        return cancelledList;
    }

    public ArrayList<User> getFinalList(){
        return finalList;
    }

    public long getEventName(){
        return eventName;
    }

    public long getDate(){
        return date;
    }

    public long getRegistrationDeadline(){
        return registrationDeadline;
    }

    public String getLocation(){
        return location;
    }

    public long getTotalSpots(){
        return totalSpots;
    }
    public String getDescription(){
        return description;
    }

    public long getEntrantsLimit(){
        return entrantsLimit;
    }

    public long getSampleSize() {
        return sampleSize;
    }
}

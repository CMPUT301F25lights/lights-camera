package com.example.lotterize;

import java.util.ArrayList;

public class Notification {
    private User sender;
    private ArrayList<User> receivers;
    private String message;

    public Notification(User sender, ArrayList<User> receivers, String message){
        this.sender = sender;
        this.receivers = receivers;
        this.message = message;
    }

    public User getSender() {
        return sender;
    }

    public ArrayList<User> getReceivers() {
        return receivers;
    }

    public String getMessage() {
        return message;
    }

    public static void notifySelectedEntrants(ArrayList<User> chosen, Event event) {
        for (User u : chosen) {
            System.out.println("Notification sent to " + u.getEmail() +
                    " for event " + event.getEventName());
        }
    }
}

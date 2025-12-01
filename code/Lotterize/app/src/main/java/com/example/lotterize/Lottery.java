package com.example.lotterize;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Handles the lottery logic for selecting event participants
 * from a waitlist, drawing replacements, and sending notifications.
 */
public class Lottery {

    private final Random random;
    private final NotificationSender notificationSender;

    /** Default constructor for production usage */
    public Lottery() {
        this(new Random(), new NotificationSender());
    }

    /** Test constructor (Dependency Injection) */
    public Lottery(NotificationSender sender) {
        this(new Random(), sender);
    }

    /** Full test constructor */
    public Lottery(Random random, NotificationSender sender) {
        this.random = random;
        this.notificationSender = sender;
    }

    /**
     * Draw winners for the event based on available spots.
     * Moves winners from waitList → selectedList.
     *
     * @param event The event to process
     * @return Independent list of winner user IDs
     */
    public List<String> drawWinners(Event event) {
        ArrayList<String> waitList = event.getWaitList();
        ArrayList<String> selectedList = event.getSelectedList();
        ArrayList<String> finalList = event.getFinalList();   // <-- added

        if (waitList == null || waitList.isEmpty()) {
            return List.of();
        }

        int totalSpots = (int) event.getTotalSpots();
        int spotsRemaining = totalSpots - selectedList.size();

        if (spotsRemaining <= 0) {
            return List.of();
        }
        // Filter eligible users: remove anyone already selected or final
        ArrayList<String> eligible = new ArrayList<>();
        for (String user : waitList) {
            if (!selectedList.contains(user) && !finalList.contains(user)) {
                eligible.add(user);
            }
        }

        if (eligible.isEmpty()) {
            return List.of();
        }


        int winnersToPick = Math.min(spotsRemaining, eligible.size());

        // Shuffle only the eligible users
        Collections.shuffle(eligible, random);

        List<String> winners = new ArrayList<>(eligible.subList(0, winnersToPick));
        List<String> losers  = new ArrayList<>(eligible.subList(winnersToPick, eligible.size()));

        // Update event state
        selectedList.addAll(winners);

        // Remove winners from *waitList*, not eligible
        waitList.removeAll(winners);
        Log.d("DEBUG", "notifyUsers(): event.ownerId = " + event.getOwnerId());
        // Notifications
        sendWinnerNotifications(event, winners);
        sendLoserNotifications(event, losers);

        return winners;
    }

    /**
     * Draws a single replacement winner if available.
     *
     * @param event The event to update
     * @return Replacement winner ID, or null if none available
     */
    public String drawReplacement(Event event) {
        ArrayList<String> waitList = event.getWaitList();
        ArrayList<String> selectedList = event.getSelectedList();
        ArrayList<String> finalList = event.getFinalList();

        if (waitList == null || waitList.isEmpty()) return null;

        int totalSpots = (int) event.getTotalSpots();
        if (selectedList.size() >= totalSpots) return null;

        // Filter eligible candidates
        List<String> eligible = new ArrayList<>();
        for (String u : waitList) {
            if (!selectedList.contains(u) && !finalList.contains(u)) eligible.add(u);
        }
        if (eligible.isEmpty()) return null;

        Collections.shuffle(eligible, random);
        String replacement = eligible.get(0);

        selectedList.add(replacement);
        waitList.remove(replacement);
        Log.d("DEBUG", "notifyUsers(): event.ownerId = " + event.getOwnerId());
        sendWinnerNotifications(event, Collections.singletonList(replacement));
        return replacement;
    }



    private void sendWinnerNotifications(Event event, List<String> winners) {
        if (winners == null || winners.isEmpty()) return;

        String message = "Congratulations! You have been selected for the event: "
                + event.getEventName();

        notifyUsers(event, message, winners);
    }

    private void sendLoserNotifications(Event event, List<String> losers) {
        if (losers == null || losers.isEmpty()) return;

        String message = "Unfortunately, you were not selected for the event: "
                + event.getEventName() + ". Thank you for your interest.";

        notifyUsers(event, message, losers);
    }

    private void notifyUsers(@NonNull Event event, String message, List<String> userIds) {
        Log.d("DEBUG", "notifyUsers(): event.ownerId = " + event.getOwnerId());
        ArrayList<String> receivers = new ArrayList<>(userIds);
        String senderId = event.getOwnerId(); // should grab owner's ID for message now

        //String senderId = CurrentUser.get().getUserId();

        notificationSender.sendNotification(message, receivers);
    }
}
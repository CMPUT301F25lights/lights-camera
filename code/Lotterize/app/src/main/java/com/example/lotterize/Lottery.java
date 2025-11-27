package com.example.lotterize;

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

        if (waitList == null || waitList.isEmpty()) {
            return List.of();
        }

        int totalSpots = (int) event.getTotalSpots();
        int spotsRemaining = totalSpots - selectedList.size();

        if (spotsRemaining <= 0) {
            return List.of();
        }

        int winnersToPick = Math.min(spotsRemaining, waitList.size());

        // Shuffle a working copy
        ArrayList<String> shuffled = new ArrayList<>(waitList);
        Collections.shuffle(shuffled, random);

        // Safe copies of winners/losers
        List<String> winners = new ArrayList<>(shuffled.subList(0, winnersToPick));
        List<String> losers  = new ArrayList<>(shuffled.subList(winnersToPick, shuffled.size()));

        // Update event state
        selectedList.addAll(winners);
        waitList.removeAll(winners);

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

        if (waitList == null || waitList.isEmpty()) {
            return null;
        }

        // Randomly pick a replacement
        int index = random.nextInt(waitList.size());
        String winner = waitList.get(index);

        selectedList.add(winner);
        waitList.remove(index);

        sendWinnerNotifications(event, Collections.singletonList(winner));
        return winner;
    }



    private void sendWinnerNotifications(Event event, List<String> winners) {
        if (winners == null || winners.isEmpty()) return;

        String message = "Congratulations! You have been selected for the event: "
                + event.getEventName();

        notifyUsers(message, winners);
    }

    private void sendLoserNotifications(Event event, List<String> losers) {
        if (losers == null || losers.isEmpty()) return;

        String message = "Unfortunately, you were not selected for the event: "
                + event.getEventName() + ". Thank you for your interest.";

        notifyUsers(message, losers);
    }

    private void notifyUsers(String message, List<String> userIds) {
        ArrayList<String> receivers = new ArrayList<>(userIds);
        String senderId = CurrentUser.get().getUserId();

        notificationSender.sendNotification(senderId, message, receivers);
    }
}
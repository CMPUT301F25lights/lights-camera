package com.example.lotterize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Lottery {

    private final Random random;
    private final NotificationSender notificationSender;

    public Lottery() {
        this.random = new Random();
        this.notificationSender = new NotificationSender();
    }

    /**
     * Draw winners from the event's waiting list and notify them.
     *
     * @param event The event to run the lottery on
     * @param numberOfWinners Number of entrants to select
     * @return List of selected winner IDs
     */
    public List<String> drawWinners(Event event, int numberOfWinners) {
        ArrayList<String> waitList = event.getWaitList();
        ArrayList<String> selectedList = event.getSelectedList();

        if (waitList == null || waitList.isEmpty()) {
            return Collections.emptyList();
        }

        // Ensure we do not try to pick more winners than available entrants
        int winnersToPick = Math.min(numberOfWinners, waitList.size());


        ArrayList<String> shuffledList = new ArrayList<>(waitList);
        Collections.shuffle(shuffledList, random);

        // Pick the first 'winnersToPick' entrants
        List<String> winners = shuffledList.subList(0, winnersToPick);


        selectedList.addAll(winners);
        event.getFinalList().addAll(winners);
        waitList.removeAll(winners);

        // Send notifications to winners
        sendWinnerNotifications(event, winners);

        return new ArrayList<>(winners);
    }

    /**
     * Draw a replacement winner if someone declines and notify them.
     *
     * @param event The event to draw a replacement from
     * @return ID of the replacement winner, or null if none available
     */
    public String drawReplacement(Event event) {
        ArrayList<String> waitList = event.getWaitList();
        ArrayList<String> selectedList = event.getSelectedList();

        if (waitList == null || waitList.isEmpty()) {
            return null;
        }

        int index = random.nextInt(waitList.size());
        String winner = waitList.get(index);

        // Update event lists
        selectedList.add(winner);
        event.getFinalList().add(winner);
        waitList.remove(index);

        // Send notification to replacement winner
        sendWinnerNotifications(event, Collections.singletonList(winner));

        return winner;
    }

    /**
     * Helper function to send notifications to a list of winners
     *
     * @param event   The event for context
     * @param winners List of winner IDs
     */
    private void sendWinnerNotifications(Event event, List<String> winners) {
        String message = "Congratulations! You have been selected for the event: " + event.getEventName();

        ArrayList<String> receiversIds = new ArrayList<>(winners);

        // Send notification using NotificationSender
        notificationSender.sendNotification(CurrentUser.get().getUserId(), message, receiversIds);
    }
}
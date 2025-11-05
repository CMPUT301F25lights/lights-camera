package com.example.lotterize;

import com.example.lotterize.Event;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * This class is used as a controller for my Lottery model which
 */
public class LotteryController {

    private final FirebaseFirestore db;
    private final Lottery lottery;


    public LotteryController() {
        this.db = FirebaseFirestore.getInstance();
        this.lottery = new Lottery();
    }
    /**
     * Run the lottery for a specific event once registration closes.
     * @param event
     */
    public void runLottery(Event event) {
        List<String> winners = lottery.drawWinners(event);

        // Update Firestore with all modified lists (winners and losers)
        updateEventInFirestore(event);
    }

    /**
     * Handles when a selected user accepts their invitation.
     * Moves them from selectedList to finalList.
     */
    public void acceptInvitation(Event event, String userId) {
        if (event.getSelectedList().contains(userId)) {
            event.getSelectedList().remove(userId);
            event.getFinalList().add(userId);
            updateEventInFirestore(event);
        }
    }

    /**
     * Handles when a selected user declines their invitation.
     * Moves them to cancelledList, and possibly replaces them
     * with a new user from the waitlist.
     */
    public void declineInvitation(Event event, String userId) {
        if (event.getSelectedList().contains(userId)) {
            event.getSelectedList().remove(userId);
            event.getCancelledList().add(userId);

            // Optional: replace with next person on waitlist
            if (!event.getWaitList().isEmpty()) {
                String nextUser = event.getWaitList().remove(0);
                event.getSelectedList().add(nextUser);
            }
            updateEventInFirestore(event);
        }
    }

    /**
     * Pushes updated event data to Firestore. Needs some work still too
     *
     */
    private void updateEventInFirestore(Event event) {
        db.collection("events")
                .document(event.getEventId())
                .set(event)
                .addOnSuccessListener(aVoid -> {
                    // success log or callback
                    System.out.println("Event updated successfully.");
                })
                .addOnFailureListener(e -> {
                    // handle error
                    System.err.println("Failed to update event: " + e.getMessage());
                });
    }

    /** Need to add this in notifications most likely or events registered
     * Button acceptButton = findViewById(R.id.accept_button);
     * Button declineButton = findViewById(R.id.decline_button);
     *
     * LotteryController controller = new LotteryController();
     *
     * acceptButton.setOnClickListener(v -> controller.acceptInvitation(event, currentUserId));
     * declineButton.setOnClickListener(v -> controller.declineInvitation(event, currentUserId));
     */
}
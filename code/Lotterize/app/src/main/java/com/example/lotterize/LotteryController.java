package com.example.lotterize;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class LotteryController {

    private final FirebaseFirestore db;
    private final Lottery lottery;

    // Production constructor
    public LotteryController() {
        this(FirebaseFirestore.getInstance(), new Lottery());
    }

    // Constructor for tests (mocked DB + optional mocked Lottery)
    public LotteryController(FirebaseFirestore db) {
        this(db, new Lottery());
    }

    // Full dependency injection constructor
    public LotteryController(FirebaseFirestore db, Lottery lottery) {
        this.db = db;
        this.lottery = lottery;
    }

    public void runLottery(Event event) {
        if (event.getEventId() == null || event.getEventId().isEmpty()) {
            System.err.println("Event ID is null, cannot update Firestore");
            return;
        }

        List<String> winners = lottery.drawWinners(event);
        updateEventInFirestore(event);
    }

    public void acceptInvitation(Event event, String userId) {
        if (!event.getSelectedList().contains(userId)) return;

        int totalSpots = (int) event.getTotalSpots();

        // Prevent overfilling final list
        if (event.getFinalList().size() >= totalSpots) return;


        if (!event.getFinalList().contains(userId)) {
            event.getFinalList().add(userId);
        }

        updateEventInFirestore(event);
    }

    public void declineInvitation(Event event, String userId) {
        //System.out.println("Decline called for user: " + userId);

        if (!event.getSelectedList().contains(userId)) return;

        // Removes the user who declines
        event.getSelectedList().remove(userId);
        event.getCancelledList().add(userId);


        // Draw one replacement
        String replacement = lottery.drawReplacement(event);
        //System.out.println("Selected List: " + event.getSelectedList());

        updateEventInFirestore(event);
        //System.out.println("Selected List: " + event.getSelectedList());
    }

    // Internal helper for Firestore update (can be mocked)
    protected void updateEventInFirestore(Event event) {
        db.collection("events")
                .document(event.getEventId())
                .set(event);
    }
}
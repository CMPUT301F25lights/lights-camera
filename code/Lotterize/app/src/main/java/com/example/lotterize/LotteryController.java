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
        List<String> winners = lottery.drawWinners(event);
        updateEventInFirestore(event);
    }

    public void acceptInvitation(Event event, String userId) {
        if (event.getSelectedList().contains(userId)) {
            event.getSelectedList().remove(userId);
            event.getFinalList().add(userId);
            updateEventInFirestore(event);
        }
    }

    public void declineInvitation(Event event, String userId) {
        if (event.getSelectedList().contains(userId)) {
            event.getSelectedList().remove(userId);
            event.getCancelledList().add(userId);

            if (!event.getWaitList().isEmpty()) {
                String nextUser = event.getWaitList().remove(0);
                event.getSelectedList().add(nextUser);
            }
            updateEventInFirestore(event);
        }
    }

    // Internal helper for Firestore update (can be mocked)
    protected void updateEventInFirestore(Event event) {
        db.collection("events")
                .document(event.getEventId())
                .set(event);
    }
}
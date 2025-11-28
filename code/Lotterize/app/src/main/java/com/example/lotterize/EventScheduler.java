package com.example.lotterize;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Class used to trigger lottery
 */
public class EventScheduler {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final LotteryController lotteryController;

    public EventScheduler() {
        this.lotteryController = new LotteryController();
    }

    public EventScheduler(LotteryController lotteryController) {
        this.lotteryController = lotteryController;
    }

    /**
     * Monitors registration deadline to trigger the lottery
     */
    public void monitorEvents() {
        db.collection("events").addSnapshotListener((value, error) -> {
            if (error != null || value == null) return;

            for (DocumentSnapshot doc : value.getDocuments()) {
                if (!doc.exists()) continue; // skip deleted docs

                Event event = doc.toObject(Event.class);

                if (event == null) continue; // skip if event is null

                event.setEventId(doc.getId()); // make sure we have the Firestore ID

                if (event.getRegistrationDeadline() != null &&
                        Timestamp.now().compareTo(event.getRegistrationDeadline()) >= 0) {
                        lotteryController.runLottery(event); // delegate logic
                }
            }
        });
    }

    /**
     * runs the lottery
     * @param event
     */
    /*
    private void runLottery(Event event) {
        Lottery lottery = new Lottery();
        List<String> winners = lottery.drawWinners(event);

        // Update event in Firestore (not going to change anything in db rn)
        db.collection("events").document(event.getEventId()).set(event);
    }*/
}


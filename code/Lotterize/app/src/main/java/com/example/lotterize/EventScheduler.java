package com.example.lotterize;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is intended to basically be used to trigger the lottery by monitoring the
 * event reg. deadline.
 */
public class EventScheduler {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * This method is used to monitor when the event registration is over and trigger the lottery
     */
    public void monitorEvents() {
        db.collection("events").addSnapshotListener((value, error) -> {
            if (error != null || value == null) return;

            for (DocumentSnapshot doc : value.getDocuments()) {
                Event event = doc.toObject(Event.class);
                if (event != null && Timestamp.now().compareTo(event.getRegistrationDeadline()) >= 0) {
                    runLottery(event);
                }
            }
        });
    }

    /**
     * Runs the lottery through the controller
     * @param event
     */
    private void runLottery(Event event) {
        //Lottery lottery = new Lottery();
        //List<String> winners = lottery.drawWinners(event);
        LotteryController controller = new LotteryController();
        controller.runLottery(event);

        // Update event in Firestore (not going to change anything in db rn)
        //db.collection("events").document(event.getEventId()).set(event);
    }
}


package com.example.lotterize;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EventScheduler {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

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

    private void runLottery(Event event) {
        Lottery lottery = new Lottery();
        List<String> winners = lottery.drawWinners(event);

        // Update event in Firestore (not going to change anything in db rn)
        //db.collection("events").document(event.getEventId()).set(event);
    }
}


package com.example.lotterize;
import android.util.Log;

import com.example.lotterize.Notification;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class NotificationSender {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void sendNotification(long senderId, String message, ArrayList<Long> receiversIds) {

        Long notificationId = null;
        Timestamp now = Timestamp.now();

        Notification notif = new Notification(notificationId, senderId, message, now, receiversIds);

        db.collection("notifications")
                .add(notif)
                .addOnSuccessListener(docRef -> {
                    Log.d("NotificationSender", "Notification sent: ");
                })
                .addOnFailureListener(e -> {
                    Log.e("NotificationSender", "Failed to send notification", e);
                });
    }
    public void sendNotification(Notification notification) {

        db.collection("notifications")
                .add(notification)
                .addOnSuccessListener(docRef -> {
                    Log.d("NotificationSender", "Notification sent: ");
                })
                .addOnFailureListener(e -> {
                    Log.e("NotificationSender", "Failed to send notification", e);
                });
    }
}
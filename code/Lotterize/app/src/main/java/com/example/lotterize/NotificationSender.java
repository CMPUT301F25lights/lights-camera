package com.example.lotterize;
import android.util.Log;

import com.example.lotterize.Notification;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class NotificationSender {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void sendNotification(String senderId, String message, ArrayList<String> receiversIds) {
        DocumentReference docRef = db.collection("notifications").document();

        //Initialize a notification object with notificationId obtained from the db
        Notification notif = new Notification(docRef.getId(), senderId, CurrentUser.get().getUsername(), message, Timestamp.now(), receiversIds);

        docRef.set(notif)
                .addOnSuccessListener(v -> Log.d("NotificationSender", "Notification sent"))
                .addOnFailureListener(e -> Log.e("NotificationSender", "Failed to send notification", e));
    }
    public void sendNotification(Notification notification) {
        DocumentReference docRef = db.collection("notifications").document();
        notification.setSenderName(CurrentUser.get().getUsername());

        if (notification.getSenderId() == null || notification.getSenderId().isEmpty()){
            notification.setNotificationId(CurrentUser.get().getUserId());
        }

        notification.setNotificationId(docRef.getId());

        docRef.set(notification)
                .addOnSuccessListener(v -> Log.d("NotificationSender", "Notification sent"))
                .addOnFailureListener(e -> Log.e("NotificationSender", "Failed to send notification", e));

    }
}
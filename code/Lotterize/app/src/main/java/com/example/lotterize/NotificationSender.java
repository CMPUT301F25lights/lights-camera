package com.example.lotterize;
import android.util.Log;

import com.example.lotterize.Notification;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * This is a helper class that sends {@link Notification} objects to Firestore.
 * It creates a document under the {@code notifications} collection.
 */
public class NotificationSender {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();


    /**
     * This creates and sends a notification using the provided fields.
     * It generates a new document id in Firestore and uses it as the notification id.
     *
     * @param senderId
     *      The user id of the sender
     * @param message
     *      The contents/message body of the notification
     * @param receiversIds
     *      The list of user ids who will receive the notification
     */
    public void sendNotification(String senderId, String message, ArrayList<String> receiversIds) {
        DocumentReference docRef = db.collection("notifications").document();


        //Initialize a notification object with notificationId obtained from the db
        Notification notif = new Notification(docRef.getId(), senderId, CurrentUser.get().getName(), message, Timestamp.now(), receiversIds);

        docRef.set(notif)
                .addOnSuccessListener(v -> Log.d("NotificationSender", "Notification sent"))
                .addOnFailureListener(e -> Log.e("NotificationSender", "Failed to send notification", e));
    }

    /**
     * This sends a pre-built {@link Notification} object.
     * If the sender name is not set, it will be populated from {@code CurrentUser}.
     * If the notification does not yet have an id, a new document is created and its id is assigned.
     *
     * @param notification
     *      The notification object to send
     */
    public void sendNotification(Notification notification) {
        DocumentReference docRef = db.collection("notifications").document();

        if (notification.getSenderId() == null || notification.getSenderId().isEmpty()) {
            notification.setSenderId(CurrentUser.get().getUserId());
        }
        if (notification.getSenderName() == null || notification.getSenderName().isEmpty()) {
            notification.setSenderName(CurrentUser.get().getName() != null ? CurrentUser.get().getName():"Unknown");
        }
        if (notification.getTime() == null) {
            notification.setTime(Timestamp.now());
        }

        // Assign a new doc id if missing
        if (notification.getNotificationId() == null || notification.getNotificationId().isEmpty()) {
            notification.setNotificationId(docRef.getId());
        }

        notification.setNotificationId(docRef.getId());

        docRef.set(notification)
                .addOnSuccessListener(v -> Log.d("NotificationSender", "Notification sent"))
                .addOnFailureListener(e -> Log.e("NotificationSender", "Failed to send notification", e));
    }
}
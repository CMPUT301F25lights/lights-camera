package com.example.lotterize;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * Repository responsible for loading {@link Notification} documents from Firestore.
 * Implemented as a singleton so production code and tests share a single entry point.
 */
public class NotificationsRepository {

    private static NotificationsRepository instance = new NotificationsRepository();
    protected final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Callback for notification queries.
     */
    public interface NotificationsCallback {
        void onSuccess(@NonNull ArrayList<Notification> notifications);
        void onError(@NonNull Exception e);
    }

    public static NotificationsRepository getInstance() {
        return instance;
    }

    public static void setInstance(NotificationsRepository repo) {
        instance = repo;
    }

    /**
     * Loads notifications where the given userId is in receiversId.
     */
    public void fetchNotificationsReceived(@NonNull String userId,
                                           @NonNull NotificationsCallback callback) {
        db.collection("notifications")
                .whereArrayContains("receiversId", userId)
                .get()
                .addOnSuccessListener(qs -> {
                    ArrayList<Notification> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : qs) {
                        list.add(toNotification(doc));
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Loads notifications where senderId == userId.
     */
    public void fetchNotificationsSent(@NonNull String userId,
                                       @NonNull NotificationsCallback callback) {
        db.collection("notifications")
                .whereEqualTo("senderId", userId)
                .get()
                .addOnSuccessListener(qs -> {
                    ArrayList<Notification> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : qs) {
                        list.add(toNotification(doc));
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Helper to map Firestore document -> Notification model.
     */
    @NonNull
    protected Notification toNotification(@NonNull QueryDocumentSnapshot doc) {
        String notificationId = doc.getString("notificationId");
        String message        = doc.getString("message");
        String senderName     = doc.getString("senderName");
        String senderId       = doc.getString("senderId");
        Timestamp timestamp   = doc.getTimestamp("time");
        ArrayList<String> receiversId =
                (ArrayList<String>) doc.get("receiversId");

        return new Notification(
                notificationId,
                senderId,
                senderName,
                message,
                timestamp,
                receiversId != null ? receiversId : new ArrayList<>()
        );
    }
}

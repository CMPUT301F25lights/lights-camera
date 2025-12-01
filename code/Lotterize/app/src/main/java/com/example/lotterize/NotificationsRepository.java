package com.example.lotterize;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * Repository responsible for fetching {@link Notification} documents from Firestore.
 *
 * <p>This class abstracts all Firestore access related to notifications, providing
 * simple methods for retrieving notifications sent by or received by a specific user.
 * It is implemented as a singleton so that production code, view models, and tests
 * can all work with the same shared instance or provide a mocked one via
 * {@link #setInstance(NotificationsRepository)}.</p>
 *
 * <p>All Firestore operations execute asynchronously and return results through
 * the {@link NotificationsCallback} interface.</p>
 */
public class NotificationsRepository {

    private static NotificationsRepository instance = new NotificationsRepository();
    protected final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Callback interface used to deliver asynchronous notification query results.
     *
     * <p>Both methods in this callback are guaranteed to be invoked exactly once
     * per request—either {@link #onSuccess(ArrayList)} when the Firestore query
     * completes successfully or {@link #onError(Exception)} when an error occurs.</p>
     */
    public interface NotificationsCallback {

        /**
         * Called when notification data has been successfully retrieved.
         *
         * @param notifications A list of {@link Notification} objects produced from Firestore.
         */
        void onSuccess(@NonNull ArrayList<Notification> notifications);

        /**
         * Called when a Firestore read operation fails.
         *
         * @param e The exception describing the failure.
         */
        void onError(@NonNull Exception e);
    }

    /**
     * Returns the shared singleton repository instance.
     *
     * <p>This method is used throughout the app to provide centralized
     * access to Firestore notification queries.</p>
     *
     * @return The global {@link NotificationsRepository} instance.
     */
    public static NotificationsRepository getInstance() {
        return instance;
    }

    /**
     * Overrides the static repository instance.
     *
     * <p>Primarily used for dependency injection in unit tests,
     * allowing mocked repository behavior.</p>
     *
     * @param repo The replacement repository instance.
     */
    public static void setInstance(NotificationsRepository repo) {
        instance = repo;
    }

    /**
     * Fetches all notifications where the given user appears in the
     * {@code receiversId} array field.
     *
     * <p>This query is used for retrieving notifications *received* by a user.
     * Each resulting Firestore document is converted into a {@link Notification}
     * model via {@link #toNotification(QueryDocumentSnapshot)}.</p>
     *
     * @param userId   The ID of the user who received the notifications.
     * @param callback Callback receiving the resulting list or error.
     */
    public void fetchNotificationsReceived(
            @NonNull String userId,
            @NonNull NotificationsCallback callback
    ) {
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
     * Fetches all notifications where the {@code senderId} field equals the given user.
     *
     * <p>This query is used to load all notifications *sent* by a specific user,
     * typically to populate an admin UI or user history screen.</p>
     *
     * @param userId   The ID of the user who sent the notifications.
     * @param callback Callback receiving the resulting list or error.
     */
    public void fetchNotificationsSent(
            @NonNull String userId,
            @NonNull NotificationsCallback callback
    ) {
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
     * Converts a Firestore notification document into a {@link Notification} model object.
     *
     * <p>Each required or optional Firestore field is retrieved and mapped to its
     * corresponding constructor parameter. Missing {@code receiversId} fields default
     * to an empty list to ensure consistent model behavior.</p>
     *
     * @param doc The Firestore document snapshot representing a notification.
     * @return A fully constructed {@link Notification} containing the document data.
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

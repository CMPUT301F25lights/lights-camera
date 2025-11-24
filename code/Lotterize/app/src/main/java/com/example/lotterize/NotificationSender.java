package com.example.lotterize;
import android.util.Log;

import com.example.lotterize.Notification;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a helper class responsible for sending {@link Notification} objects to Firestore.
 * <p>
 * It supports:
 * <ul>
 *     <li>Sending notifications from primitive fields
 *         (sender id, message, list of candidate receiver ids),</li>
 *     <li>Filtering candidate receivers based on each user's {@code wantNotification} flag
 *         in the {@code users} collection,</li>
 *     <li>Writing a new document into the {@code notifications} collection,</li>
 *     <li>Optionally reporting the number of recipients via a callback.</li>
 * </ul>
 */

public class NotificationSender {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Callback interface for callers that want to be notified when a notification send
     * operation finishes.
     */
    public interface NotificationCallback {

        /**
         * This is called when the send operation finishes successfully.
         *
         * @param count the number of recipients that actually received the notification
         *              after filtering by {@code wantNotification}
         */
        void onComplete(int count);

        /**
         * This is called when the send operation fails due to an error (e.g., Firestore failure).
         *
         * @param e the exception describing the failure
         */
        void onError(Exception e);
    }

    /**
     * This sends a notification to the given list of candidate receivers, with an optional callback
     * to report the result.
     * <p>
     * This method:
     * <ol>
     *     <li>Chunks {@code receiversIds} into groups of at most 10 ids (due to Firestore
     *         {@code whereIn} limits),</li>
     *     <li>Queries the {@code users} collection for each chunk to check the
     *         {@code wantNotification} flag,</li>
     *     <li>Builds a final list of recipients that have not opted out,</li>
     *     <li>If at least one recipient remains, writes a new document into the
     *         {@code notifications} collection,</li>
     *     <li>Invokes {@link NotificationCallback#onComplete(int)} with the number of
     *         recipients, or {@link NotificationCallback#onError(Exception)} on failure.</li>
     * </ol>
     *
     * @param senderId     user id of the sender
     * @param message      body or content of the notification
     * @param receiversIds list of candidate user ids who should receive the notification
     * @param callback     optional callback invoked when the operation completes or fails;
     *                     may be {@code null} if the caller does not care about the result
     */
    public void sendNotification(String senderId, String message, ArrayList<String> receiversIds, NotificationCallback callback) {
        if (receiversIds == null || receiversIds.isEmpty()) {
            Log.d("SendNotification", "sendNotification: no candidate receivers.");
            if (callback != null) callback.onComplete(0);
            return;
        }

        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (int i = 0; i < receiversIds.size(); i += 10) {
            List<String> chunk = receiversIds.subList(i, Math.min(i + 10, receiversIds.size()));
            tasks.add(db.collection("users").whereIn(FieldPath.documentId(), chunk).get());
        }

        Tasks.whenAllSuccess(tasks)
                .addOnSuccessListener(results -> {
                    ArrayList<String> finalRecipients = new ArrayList<>();

                    for (Object r : results) {
                        QuerySnapshot qs = (QuerySnapshot) r;
                        for (DocumentSnapshot snap : qs.getDocuments()) {
                            Boolean wantNotification = snap.getBoolean("wantNotification");
                            if (wantNotification == null || wantNotification) {
                                finalRecipients.add(snap.getId());
                            }
                        }
                    }

                    int count = finalRecipients.size();

                    if (count == 0) {
                        Log.d("SendNotification", "sendNotification: no recipients after wantNotification filter.");
                        if (callback != null) callback.onComplete(0);
                        return;
                    }

                    SendNotificationWithFilterReceivers(senderId, message, finalRecipients);

                    if (callback != null) callback.onComplete(count);
                })
                .addOnFailureListener(e -> {
                    Log.e("SendNotification", "sendNotification: failed to resolve wantNotification flags.", e);
                    if (callback != null) callback.onError(e);
                });
    }

    /**
     * This method is an overload of {@link #sendNotification(String, String, ArrayList, NotificationCallback)}
     * that ignores the result and does not use a callback.
     * <p>
     * This is useful for callers that only care about triggering the send and do not need to
     * know how many recipients actually received the notification or whether it failed.
     *
     * @param senderId     user id of the sender
     * @param message      body or content of the notification
     * @param receiversIds list of candidate user ids who should receive the notification
     */
    public void sendNotification(String senderId, String message, ArrayList<String> receiversIds) {
        sendNotification(senderId, message, receiversIds, null);
    }

    /**
     *
     * This method assumes that receiversIds contains only users who have not
     * opted out of notifications. It generates a new document in the
     * "notifications" collection, uses the document ID as the notification ID,
     * and sets the sender's display name from {@link CurrentUser}.
     *
     * @param senderId
     *         user id of the sender
     * @param message
     *         body or content of the notification
     * @param receiversIds
     *         final list of recipient user ids after wantNotification filtering
     */
    private void SendNotificationWithFilterReceivers(String senderId, String message, ArrayList<String> receiversIds) {
        DocumentReference docRef = db.collection("notifications").document();

        String senderName = "";
        if (CurrentUser.get().getName() == null) {
            senderName = "Username: " + CurrentUser.get().getUsername();
        } else {
            senderName = CurrentUser.get().getName();
        }

        Notification notif = new Notification(docRef.getId(), senderId, senderName, message, Timestamp.now(), receiversIds);

        docRef.set(notif)
                .addOnSuccessListener(v -> Log.d("SendNotification", "Notification sent"))
                .addOnFailureListener(e -> Log.e("SendNotification", "Failed to send notification", e));
    }

    /**
     * Sends a pre-built {@link Notification}.
     *
     * Missing fields are filled in as follows:
     * - senderId is set from {@link CurrentUser} if it is null or empty.
     * - senderName is set from the current user's name or username if it is null or empty.
     * - time is set to the current timestamp if it is null.
     *
     * Candidate receiver IDs are filtered using the wantNotification flag in the
     * "users" collection. If notificationId is null or empty, a new document is
     * created in the "notifications" collection and its ID is stored in the
     * notification. If no recipients remain after filtering, nothing is written.
     *
     * @param notification
     *         notification object to send.
     */
    public void sendNotification(Notification notification) {
        if (notification.getSenderId() == null || notification.getSenderId().isEmpty()) {
            notification.setSenderId(CurrentUser.get().getUserId());
        }
        if (notification.getSenderName() == null || notification.getSenderName().isEmpty()) {
            String senderName = CurrentUser.get().getName() != null ? CurrentUser.get().getName() : "Username: " + CurrentUser.get().getUsername();
            notification.setSenderName(senderName);
        }
        if (notification.getTime() == null) {
            notification.setTime(Timestamp.now());
        }

        ArrayList<String> candidateIds = notification.getReceiversId();
        if (candidateIds == null || candidateIds.isEmpty()) {
            Log.d("NotificationSender", "sendNotification(Notification): no candidate receivers.");
            return;
        }

        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (int i = 0; i < candidateIds.size(); i += 10) {
            List<String> chunk = candidateIds.subList(i, Math.min(i + 10, candidateIds.size()));
            tasks.add(db.collection("users").whereIn(FieldPath.documentId(), chunk).get());
        }

        Tasks.whenAllSuccess(tasks)
                .addOnSuccessListener(results -> {
                    ArrayList<String> finalRecipients = new ArrayList<>();

                    for (Object r : results) {
                        QuerySnapshot qs = (QuerySnapshot) r;
                        for (DocumentSnapshot snap : qs.getDocuments()) {
                            Boolean wantNotification = snap.getBoolean("wantNotification");

                            if (wantNotification == null || wantNotification) {
                                finalRecipients.add(snap.getId());
                            }
                        }
                    }

                    if (finalRecipients.isEmpty()) {
                        Log.d("NotificationSender", "sendNotification(Notification): no recipients after wantNotification filter.");
                        return;
                    }

                    notification.setReceiversId(finalRecipients);

                    DocumentReference docRef;
                    if (notification.getNotificationId() == null
                            || notification.getNotificationId().isEmpty()) {
                        docRef = db.collection("notifications").document();
                        notification.setNotificationId(docRef.getId());
                    } else {
                        docRef = db.collection("notifications")
                                .document(notification.getNotificationId());
                    }

                    docRef.set(notification)
                            .addOnSuccessListener(v ->
                                    Log.d("NotificationSender", "Notification sent"))
                            .addOnFailureListener(e ->
                                    Log.e("NotificationSender", "Failed to send notification", e));
                })
                .addOnFailureListener(e -> {
                    Log.e("NotificationSender",
                            "sendNotification(Notification): failed to resolve wantNotification flags.", e);
                });
    }

}
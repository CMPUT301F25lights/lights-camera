package com.example.lotterize.ui.notifications;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.lotterize.CurrentUser;
import com.example.lotterize.MainActivity;
import com.example.lotterize.Notification;
import com.example.lotterize.NotificationSender;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import kotlin.contracts.Returns;

/**
 * This is a ViewModel that provides notification data to the notifications screen.
 * It owns the Firestore snapshot listener, transforms Firestore documents into
 * {@link Notification} objects, and exposes them via {@link LiveData}.
 */
public class NotificationsViewModel extends ViewModel {

    /** LiveData holding the current list of notifications for the signed-in user. */
    private final MutableLiveData<ArrayList<Notification>> notificationsLiveData =
            new MutableLiveData<>(new ArrayList<>());
    private final NotificationSender sender = new NotificationSender();

    /** Firestore listener registration used to remove the listener when cleared. */
    private ListenerRegistration registration;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final MutableLiveData<String> toast = new MutableLiveData<>();
    private final String currentUserId = CurrentUser.get().getUserId();


    /**
     * This is the constructor for the ViewModel.
     * It initializes the Firestore listener to begin receiving updates.
     */
    public NotificationsViewModel() {
        startListening();
    }

    /**
     * This fetches the event’s recipient list (by {@code listStatus}) from Firestore
     * and sends {@code message} to those user IDs via {@code NotificationSender}.
     * Posts a short status to {@code toast} (not found / empty / sent / failure).
     *
     * @param eventId   Event document ID
     * @param listStatus Field name holding recipients
     * @param message   The message of Notification
     * @param senderId  ID of the user sending the notification
     */
    public void sendToStatus(String eventId, String listStatus, String message, String senderId) {
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap == null || !snap.exists()) {
                        toast.postValue("Event not found");
                        return;
                    }

                    // Read the array field (e.g., "WAITLIST", "CHOSEN", etc.)
                    Object raw = snap.get(listStatus);

                    ArrayList<String> ids = new ArrayList<>();
                    if (raw instanceof java.util.List) {
                        for (Object o : (java.util.List<?>) raw) {
                            if (o != null) ids.add(String.valueOf(o));
                        }
                    }

                    if (ids.isEmpty()) {
                        toast.postValue("No recipients for " + listStatus.toLowerCase());
                        return;
                    }

                    sender.sendNotification(senderId, message, ids);
                    toast.postValue("Sent to " + ids.size() + " " + listStatus.toLowerCase() + " entrant(s)");
                })
                .addOnFailureListener(e -> {
                    toast.postValue("Failed to load recipients: " + e.getMessage());
                });
    }

    /**
     * This starts a Firestore snapshot listener that loads notifications where
     * the current user id appears in the {@code receiversId} array.
     * It converts each document into a {@link Notification} and updates LiveData.
     * Note: The query can be ordered by time.
     */
    private void startListening() {
        registration = db.collection("notifications")
                .whereArrayContains("receiversId", currentUserId)
                //.orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error)->{

                        if (error != null) {
                            Log.e("FireStore", error.toString());
                            return;
                        }

                        ArrayList<Notification> notifList = new ArrayList<>();

                        if (value != null && !value.isEmpty()) {
                            for (QueryDocumentSnapshot snapshot : value){

                                String notificationId = snapshot.getString("notificationId");
                                String senderId = snapshot.getString("senderId");
                                String senderName = snapshot.getString("senderName");
                                String message = snapshot.getString("message");
                                Timestamp time = snapshot.getTimestamp("time");

                                // receiversId is stored as an array in Firestore
                                @SuppressWarnings("unchecked")
                                ArrayList<String> receiversId = (ArrayList<String>) snapshot.get("receiversId");
                                if (receiversId == null) {
                                    receiversId = new ArrayList<>();
                                }

                                Notification notification = new Notification(notificationId, senderId, senderName, message, time, receiversId);
                                notifList.add(notification);
                            }
                            notifList.sort((notif1, notif2) ->{
                                if (notif1.getTime() == null && notif2.getTime() == null) return 0;
                                if (notif1.getTime() == null) return 1;
                                else if (notif2.getTime() == null){ return -1; }
                                else{
                                    return notif2.getTime().compareTo(notif1.getTime());
                                }
                            });
                            notificationsLiveData.setValue(notifList);
                        }

                });
    }

    /**
     * This returns the LiveData that provides the list of notifications
     * for the current user.
     * @return
     *      Returns a {@link LiveData} of {@code ArrayList<Notification>} that the UI can observe
     */
    public LiveData<ArrayList<Notification>> getNotifications() {
        return notificationsLiveData;
    }


    /**
     * This removes the Firestore snapshot listener when the ViewModel is about to be destroyed
     * to prevent memory leaks.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        //Check to prevent leaking the snapshot listener when Fragment goes away
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }

    /**
     * This shows a short toast message.
     *
     * @return toast - show whether we successfully send notifications or not
     */
    public LiveData<String> toast() { return this.toast; }
}

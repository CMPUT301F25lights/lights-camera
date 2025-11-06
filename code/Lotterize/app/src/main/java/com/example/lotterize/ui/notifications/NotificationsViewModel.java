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


    /** Firestore listener registration used to remove the listener when cleared. */
    private ListenerRegistration registration;

    private final String currentUserId = CurrentUser.get().getUserId();

    /**
     * This is the constructor for the ViewModel.
     * It initializes the Firestore listener to begin receiving updates.
     */
    public NotificationsViewModel() {
        startListening();
    }


    /**
     * This starts a Firestore snapshot listener that loads notifications where
     * the current user id appears in the {@code receiversId} array.
     * It converts each document into a {@link Notification} and updates LiveData.
     * Note: The query can be ordered by time.
     */
    private void startListening() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

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
}

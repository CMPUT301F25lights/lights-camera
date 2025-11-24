package com.example.lotterize.ui.admin.adminNotifications;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.lotterize.Notification;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * ViewModel that provides a LiveData list of all notifications
 * for the admin notification log screen.
 *
 * This ViewModel:
 * - Listens to the "notifications" collection in Firestore.
 * - Converts each document into a Notification object.
 * - Exposes the list via LiveData so the UI can observe changes.
 */
public class AdminNotificationsViewModel extends ViewModel {

    private final MutableLiveData<ArrayList<Notification>> notificationsLiveData =
            new MutableLiveData<>(new ArrayList<>());

    private ListenerRegistration registration;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();



    /**
     * This is the constructor for the ViewModel.
     * It initializes the Firestore listener to begin receiving updates.
     */
    public AdminNotificationsViewModel() {
        startListening();
    }

    /**
     * This starts a Firestore snapshot listener that loads all notifications
     * documents from the "notifications" collection.
     *
     * The query is ordered by the "time" field in descending order
     * so that the newest notifications appear first.
     * Each document is converted into a Notification object and
     * the resulting list is exposed via LiveData.
     */
    private void startListening() {
        registration = db.collection("notifications")
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

                        if (!value.isEmpty()) {
                            notificationsLiveData.setValue(notifList);
                        } else {
                            notificationsLiveData.setValue(new ArrayList<>());
                        }                    }

                });
    }

    /**
     * This returns the LiveData that provides the list of all notifications.
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

package com.example.lotterize.ui.notifications;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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

/**
 * ViewModel for the Notifications screen.
 * It owns Firestore listener and transform FireStore documents into Java Object
 * It also exposes LiveData to the Fragment.
 *
 */
public class NotificationsViewModel extends ViewModel {

    //initialize live data list of notification
    //This list stores the data got from the database
    private final MutableLiveData<ArrayList<Notification>> notificationsLiveData =
            new MutableLiveData<>(new ArrayList<>());

    // We keep this so we can stop listening when ViewModel is destroyed
    private ListenerRegistration registration;

    //Might need a function to get this value
    //Sth like using the email that user entered to sign in -> Check database -> Get the userID
    private final long currentUserId = 1L;

    public NotificationsViewModel() {
        startListening();
    }


    private void startListening() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        registration = db.collection("notifications")
                .whereArrayContains("receiversId", currentUserId)
                .orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error)->{

                        if (error != null) {
                            Log.e("FireStore", error.toString());
                            return;
                        }

                        ArrayList<Notification> notiList = new ArrayList<>();

                        if (value != null && !value.isEmpty()) {
                            for (QueryDocumentSnapshot snapshot : value){

                                //Check whether getLong return a null pointer object
                                Long notificationIdObj = snapshot.getLong("notificationId");
                                long notificationId = (notificationIdObj != null) ? notificationIdObj : 0L;

                                Long senderIdObj = snapshot.getLong("senderId");
                                long senderId = (senderIdObj != null) ? senderIdObj : 0L;

                                String message = snapshot.getString("message");
                                Timestamp time = snapshot.getTimestamp("time");

                                // receiversId is stored as an array in Firestore
                                @SuppressWarnings("unchecked")
                                ArrayList<Long> receiversId = (ArrayList<Long>) snapshot.get("receiversId");
                                if (receiversId == null) {
                                    receiversId = new ArrayList<>();
                                }

                                Notification noti = new Notification(notificationId, senderId, message, time, receiversId);
                                notiList.add(noti);
                            }
                            notificationsLiveData.setValue(notiList);
                        }

                });
    }




    public LiveData<ArrayList<Notification>> getNotifications() {
        return notificationsLiveData;
    }

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

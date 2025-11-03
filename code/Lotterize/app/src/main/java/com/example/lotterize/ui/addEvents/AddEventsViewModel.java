package com.example.lotterize.ui.addEvents;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.lotterize.CurrentUser;
import com.example.lotterize.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class AddEventsViewModel extends ViewModel {


    //initialize live data list of notification
    //This list stores the data got from the database
    private final MutableLiveData<ArrayList<Event>> myEventsLiveData =
            new MutableLiveData<>(new ArrayList<>());

    // We keep this so we can stop listening when ViewModel is destroyed
    private ListenerRegistration registration;

    private final String currentUserId = CurrentUser.get().getUserId();

    public AddEventsViewModel() {
        startListening();
    }


    private void startListening() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        registration = db.collection("events")
                .whereEqualTo("ownerId", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("FireStore", error.toString());
                        return;
                    }


                    ArrayList<Event> myEventsList = new ArrayList<>();

                    if (value != null && !value.isEmpty()) {
                        for (QueryDocumentSnapshot doc : value) {
                            try {
                                myEventsList.add(Event.addEventDetailsFromSnapShot(doc));
                            } catch (Exception e) {
                                Log.w("MyEvents", e.toString());
                            }
                        }
                        myEventsLiveData.setValue(myEventsList);
                        System.out.println(String.valueOf(myEventsList.size()));
                    }
                });
    }

    public LiveData<ArrayList<Event>> getMyEvent() {
        return myEventsLiveData;
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
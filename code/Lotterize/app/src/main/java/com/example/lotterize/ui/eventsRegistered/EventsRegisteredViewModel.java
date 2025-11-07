package com.example.lotterize.ui.eventsRegistered;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.lotterize.CurrentUser;
import com.example.lotterize.Event;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;

public class EventsRegisteredViewModel extends ViewModel {

    private final MutableLiveData<ArrayList<Event>> registeredEventsLiveData =
            new MutableLiveData<>(new ArrayList<>());

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public EventsRegisteredViewModel() {
        //loadHardcodedTestEvent(); // TEMPORARY TEST
        loadRegisteredEvents();
    }

    //test used to add event
    private void loadHardcodedTestEvent() {
        ArrayList<Event> testList = new ArrayList<>();

        testList.add(new Event(
                "TEST123",
                "OwnerID-FAKE",
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                "Test Registered Event",
                new Timestamp(new Date()),
                new Timestamp(new Date()),
                new Timestamp(new Date()),
                "Test Location",
                50L,
                "Temporary description",
                10L,
                "ABC123",
                null
        ));

        registeredEventsLiveData.setValue(testList);
        Log.d("REGISTERED DEBUG", "Hardcoded registered event loaded!");
    }

    private void loadRegisteredEvents() {

        ArrayList<String> registeredIds = CurrentUser.get().getRegisteredEventIds();
        if (registeredIds == null || registeredIds.isEmpty()) {
            registeredEventsLiveData.setValue(new ArrayList<>());
            return;
        }

        ArrayList<Event> tempList = new ArrayList<>();

        for (String eventId : registeredIds) {
            db.collection("events")
                    .document(eventId)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            Event event = snapshot.toObject(Event.class);

                            if (event != null) {
                                event.setEventId(snapshot.getId());
                                tempList.add(event);

                                registeredEventsLiveData.setValue(new ArrayList<>(tempList));
                            }
                        }
                    })
                    .addOnFailureListener(e ->
                            Log.e("EventFetch", "Failed to load: " + eventId, e)
                    );
        }
    }


    public LiveData<ArrayList<Event>> getRegisteredEvents() {
        return registeredEventsLiveData;
    }
}

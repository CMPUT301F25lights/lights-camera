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

/**
 * {@code EventsRegisteredViewModel} manages and provides the list of events
 * that the current user is registered for in the Lotterize application.
 *
 * This ViewModel handles data retrieval from Firebase Firestore, transforms it
 * into {@link Event} objects, and exposes it via {@link LiveData} so that the UI
 * can automatically update when data changes.
 */
public class EventsRegisteredViewModel extends ViewModel {

    private final MutableLiveData<ArrayList<Event>> registeredEventsLiveData =
            new MutableLiveData<>(new ArrayList<>());

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public EventsRegisteredViewModel() {
        //loadHardcodedTestEvent(); // TEMPORARY TEST
        loadRegisteredEvents();
    }

    /**
     * Loads a hardcoded test event into the LiveData for testing purposes.
     *
     * This method can be used during development to simulate a registered event
     * when Firestore data is not yet available.
     */
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

    /**
     * Loads the list of events that the current user is registered for from Firestore.
     *
     * This method retrieves the list of registered event IDs from {@link CurrentUser},
     * fetches each event document from Firestore, converts them into {@link Event}
     * objects, and updates the LiveData for observation by the UI.
     */
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


    /**
     * Returns the LiveData object containing the list of registered events.
     *
     * Observing this LiveData allows the UI to automatically refresh when
     * new registered events are loaded from Firestore.
     *
     * @return LiveData containing the list of {@link Event} objects.
     */
    public LiveData<ArrayList<Event>> getRegisteredEvents() {
        return registeredEventsLiveData;
    }
}

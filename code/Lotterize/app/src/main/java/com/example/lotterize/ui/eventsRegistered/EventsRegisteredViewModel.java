package com.example.lotterize.ui.eventsRegistered;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.lotterize.CurrentUser;
import com.example.lotterize.Event;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

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
                null,
                null,
                new ArrayList<>(),
                false,
                new HashMap<>()
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
        // Listen continuously — always reflect Firestore state
        db.collection("events").addSnapshotListener((querySnapshot, error) -> {
            if (error != null) {
                Log.e("EventsRegisteredVM", "Snapshot listener error", error);
                return;
            }
            if (querySnapshot == null) {
                registeredEventsLiveData.postValue(new ArrayList<>());
                return;
            }

            ArrayList<Event> tempList = new ArrayList<>();
            String currentUserId = CurrentUser.get().getUserId();

            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                Event event = doc.toObject(Event.class);
                if (event == null) continue;
                // Ensure the event has id set so updates map to same doc
                event.setEventId(doc.getId());

                // Guard against null lists coming from older docs
                if (event.getSelectedList() == null) event.setSelectedList(new ArrayList<>());
                if (event.getFinalList() == null) event.setFinalList(new ArrayList<>());

                if (event.getSelectedList().contains(currentUserId) ||
                        event.getFinalList().contains(currentUserId)) {
                    tempList.add(event);
                }
            }

            // Use postValue since listener might be on background thread
            registeredEventsLiveData.postValue(tempList);
        });
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

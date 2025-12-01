package com.example.lotterize.ui.addEvents;

import android.util.Log;

import androidx.annotation.NonNull;
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

/**
 * This is a ViewModel that provides the current user's events to the Add Events screen.
 * It owns a Firestore snapshot listener, transforms documents into {@link Event} objects,
 * and exposes them via {@link LiveData}.
 */
public class AddEventsViewModel extends ViewModel {


    /** LiveData holding the current user's list of events. */
    private final MutableLiveData<ArrayList<Event>> myEventsLiveData =
            new MutableLiveData<>(new ArrayList<>());

    /** Firestore listener registration used to remove the listener when cleared. */
    private ListenerRegistration registration;
    private final EventsRepository eventsRepository = EventsRepository.getInstance();

    private final String currentUserId = CurrentUser.get().getUserId();

    /**
     * This is the constructor for the ViewModel.
     * It initializes the Firestore listener to begin receiving updates.
     */
    public AddEventsViewModel() {
        startListening();
    }


    /**
     * This starts (or re-starts) a Firestore snapshot listener for the current user's events.
     *
     * The listener is scoped to documents in the {@code events} collection where
     * {@code ownerId} equals {@link #currentUserId}. Every time the query snapshot
     * changes, {@link EventsRepository} converts the documents to {@link Event}
     * instances and delivers them via {@link EventsRepository.MyEventsCallback}.
     * The resulting list is then posted into {@link #myEventsLiveData} so that
     * observers can update the UI.
     */
    private void startListening() {
                registration = EventsRepository.getInstance().listenToEvents(
                currentUserId,
                new EventsRepository.MyEventsCallback() {
                    @Override
                    public void onEvents(@NonNull ArrayList<Event> events) {
                        myEventsLiveData.setValue(events);
                    }

                    @Override
                    public void onError(@NonNull Exception e) {
                        Log.e("AddEvents", e.toString());
                    }
                }
        );
    }

    /**
     * This returns the LiveData that provides the list of events
     * for the current user.
     *
     * @return
     *      Returns a {@link LiveData} of {@code ArrayList<Event>} that the UI can observe
     */
    public LiveData<ArrayList<Event>> getMyEvent() {
        return myEventsLiveData;
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
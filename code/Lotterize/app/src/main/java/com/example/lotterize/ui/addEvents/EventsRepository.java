package com.example.lotterize.ui.addEvents;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.lotterize.Event;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
/**
 * This repository responsible for reading {@link Event} objects from Firestore.
 * <p>
 * This class is implemented as a simple singleton so that production code
 * and tests can share a single entry point for event-related Firestore access.
 * {@link #setInstance(EventsRepository)} can be used to inject a mock
 * implementation in ui tests.
 */
public class EventsRepository {

    /**
     * Singleton instance used by {@link #getInstance()}.
     */
    private static EventsRepository instance = new EventsRepository();
    protected final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Returns the shared {@link EventsRepository} instance.
     *
     * @return the singleton instance
     */
    public static EventsRepository getInstance() {
        return instance;
    }


    /**
     * This replaces the current singleton instance and mainly used in intent test
     *
     * @param eventsRepo the repository instance to use for subsequent
     *                   {@link #getInstance()} calls
     */
    public static void setInstance(EventsRepository eventsRepo) {
        instance = eventsRepo;
    }


    /**
     * This callback is used for queries that return a list of {@link Event} objects.
     */
    public interface MyEventsCallback {

        /**
         * This method is called when the repository has successfully loaded the events.
         *
         * @param events list of events owned by the requested user;
         *               may be empty if no events match
         */
        void onEvents(ArrayList<Event> events);

        /**
         * This method is called when an error occurs while loading events.
         *
         * @param e the exception describing the failure
         */
        void onError(Exception e);
    }

    /**
     * This is a Callback for queries that return a single {@link Event} and its backing
     * {@link DocumentSnapshot}.
     */
    public interface EventsDetailCallback {

        /**
         * Called when the repository receives an update for the given event.
         * If the event is loaded successfully, {@code event} and {@code doc}
         *        are non-null and {@code e} is {@code null}.
         * If an error occurs, {@code event} and {@code doc} are {@code null}
         *         and {@code e} is non-null.
         *
         * @param event the parsed {@link Event}, or {@code null} if unavailable
         * @param doc   the underlying Firestore document, or {@code null} if unavailable
         * @param e     an exception if an error occurred, or {@code null} on success
         */
        void onEvents(Event event, DocumentSnapshot doc, Exception e);

        /**
         * This is method is called when an error occurs while listening for event updates.
         *
         * @param e the exception describing the failure
         */
        void onError(@NonNull Exception e);
    }


    /**
     * This starts listening for all events owned by a specific user.
     * The query filters the {@code events} collection by {@code ownerId},
     * converts each matching document to an {@link Event} via
     * {@link Event#addEventDetailsFromSnapShot(DocumentSnapshot)}, and returns
     * the resulting list to {@link MyEventsCallback#onEvents(ArrayList)}.
     *
     * @param ownerId  the user ID of the event owner whose events should be observed
     * @param callback callback to receive updates and errors
     * @return a {@link ListenerRegistration} that can be used to stop listening
     * (via {@link ListenerRegistration#remove()})
     */
    @NonNull
    public ListenerRegistration listenToEvents(@NonNull String ownerId, @NonNull MyEventsCallback callback) {
        return db.collection("events")
                .whereEqualTo("ownerId", ownerId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.onError(error);
                        return;
                    }

                    ArrayList<Event> myEventsList = new ArrayList<>();

                    if (value != null && !value.isEmpty()) {
                        for (DocumentSnapshot doc : value) {
                            try {
                                myEventsList.add(Event.addEventDetailsFromSnapShot(doc));
                            } catch (Exception e) {
                                Log.e("AddEvents",e.toString());
                            }
                        }
                    }

                    callback.onEvents(myEventsList);
                });
    }

    /**
     * This starts listening for changes to a single event document by its ID.
     * Whenever the document changes, this method attempts to parse it into an
     * {@link Event} using {@link Event#addEventDetailsFromSnapShot(DocumentSnapshot)}
     * and invokes {@link EventsDetailCallback#onEvents(Event, DocumentSnapshot, Exception)}.
     *
     * @param eventId  the ID of the event document in the {@code events} collection
     * @param callback callback to receive the event details or errors
     * @return a {@link ListenerRegistration} that can be used to stop listening
     * (via {@link ListenerRegistration#remove()})
     */
    public ListenerRegistration listenToEventById(String eventId, EventsDetailCallback callback){
        return db.collection("events")
                .document(eventId)
                .addSnapshotListener((doc, err) -> {
                    if (err != null) {
                        callback.onEvents(null, null, err);
                        Log.e("EditEvents", err.toString());
                        return;
                    }
                    if (doc == null || !doc.exists()) {
                        callback.onEvents(null, null, err);
                        Log.e("EditEvents", err.toString());
                    }
                    try{
                        Event e = Event.addEventDetailsFromSnapShot(doc);
                        callback.onEvents(e, doc, null);
                    } catch (Exception e) {
                        callback.onEvents(null, null, e);
                    }
                });
    }

}

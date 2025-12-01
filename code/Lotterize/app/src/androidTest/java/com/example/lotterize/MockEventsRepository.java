package com.example.lotterize;

import static org.mockito.Mockito.when;

import androidx.annotation.NonNull;

import com.example.lotterize.ui.addEvents.EventsRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Objects;

import org.mockito.Mockito;

public class MockEventsRepository extends EventsRepository {
    private final ArrayList<Event> events = new ArrayList<>();

     protected Event event = new Event();

    public void addEvent(@NonNull Event e) {
        events.add(e);
    }

    @NonNull
    @Override
    public ListenerRegistration listenToEvents(@NonNull String ownerId, @NonNull MyEventsCallback callback) {
        ArrayList<Event> ownedEvents = new ArrayList<>();
        for (Event event : events) {
            if (ownerId.equals(event.getOwnerId())) {
                ownedEvents.add(event);
            }
        }

        callback.onEvents(ownedEvents);

        return new ListenerRegistration() {
            @Override
            public void remove() {
                return;
            }
        };
    }

    @NonNull
    @Override
    public ListenerRegistration listenToEventById(String eventId, EventsDetailCallback callback){
        this.event = null;
        for (Event event:events){
            if (Objects.equals(event.getEventId(), eventId)){
                this.event = event;
                break;
            }
        }

        if (this.event != null){
            DocumentSnapshot doc = Mockito.mock(DocumentSnapshot.class);

            when(doc.exists()).thenReturn(true);

            // Fields used in EditEventFragment
            when(doc.getBoolean("geolocationEnabled")).thenReturn(event.getGeolocationEnabled());
            when(doc.getString("imageUrl")).thenReturn(event.getImageUrl());
            when(doc.getString("imagePath")).thenReturn(event.getImagePath());
            when(doc.getString("eventName")).thenReturn(event.getEventName());

            // Lists used in EntrantListFragment / ChosenEntrantsListFragment
            when(doc.get("waitList")).thenReturn(event.getWaitList());
            when(doc.get("selectedList")).thenReturn(event.getSelectedList() != null ? new ArrayList<>(event.getSelectedList()) : new ArrayList<>());
            when(doc.get("cancelledList")).thenReturn(event.getCancelledList());
            when(doc.get("finalList")).thenReturn(event.getFinalList() != null ? new ArrayList<>(event.getFinalList()) : new ArrayList<>());

            callback.onEvents(event, doc, null);
        }
        return ()->{};


    }


}

package com.example.lotterize;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.lotterize.ui.eventsRegistered.EventsRegisteredViewModel;

import java.util.ArrayList;

public class FakeEventsRegisteredViewModel extends EventsRegisteredViewModel {

    private final MutableLiveData<ArrayList<Event>> liveData = new MutableLiveData<>(new ArrayList<>());

    @Override
    public LiveData<ArrayList<Event>> getRegisteredEvents() {
        return liveData;
    }

    public void emit(ArrayList<Event> events) {
        liveData.postValue(events);
    }
}

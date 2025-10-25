package com.example.lotterize.ui.eventsRegistered;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class EventsRegisteredViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public EventsRegisteredViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is events registered fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
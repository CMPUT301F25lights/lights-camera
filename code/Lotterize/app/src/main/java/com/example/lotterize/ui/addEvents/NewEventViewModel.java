package com.example.lotterize.ui.addEvents;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NewEventViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public NewEventViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is new event page");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
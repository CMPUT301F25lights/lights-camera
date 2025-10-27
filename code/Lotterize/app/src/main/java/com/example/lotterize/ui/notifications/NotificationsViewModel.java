package com.example.lotterize.ui.notifications;

import android.annotation.SuppressLint;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.auth.User;

public class NotificationsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    @SuppressLint("RestrictedApi")
    private User currentUser;

    public NotificationsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Notification");
    }



    public LiveData<String> getText() {
        return mText;
    }
}
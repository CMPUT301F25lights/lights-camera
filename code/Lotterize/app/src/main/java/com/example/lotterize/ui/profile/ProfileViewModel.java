package com.example.lotterize.ui.profile;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.lotterize.CurrentUser;
import com.example.lotterize.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileViewModel extends ViewModel {

    private static final String TAG = "ProfileViewModel";
    private final MutableLiveData<User> userLiveData;
    private final FirebaseFirestore db;
    private String currentUserId;

    public ProfileViewModel() {
        userLiveData = new MutableLiveData<>();
        db = FirebaseFirestore.getInstance();

        // Get current user from singleton
        User currentUser = CurrentUser.get();
        if (currentUser != null) {
            currentUserId = currentUser.getUserId();
            userLiveData.setValue(currentUser);
            loadUserData();
        }
    }

    private void loadUserData() {
        if (currentUserId == null) return;

        db.collection("users")
                .document(currentUserId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading user data", error);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        User user = new User(
                                snapshot.getId(),
                                snapshot.getString("name"),
                                snapshot.getString("phoneNumber"),
                                snapshot.getString("email"),
                                snapshot.getString("coordinates"),
                                snapshot.getString("username"),
                                snapshot.getString("password")
                        );
                        userLiveData.setValue(user);
                        CurrentUser.set(user); // Update singleton
                    }
                });
    }

    public void updateName(String name) {
        if (currentUserId == null) return;

        db.collection("users")
                .document(currentUserId)
                .update("name", name)
                .addOnSuccessListener(aVoid -> {
                    User user = userLiveData.getValue();
                    if (user != null) {
                        user.setName(name);
                        userLiveData.setValue(user);
                        CurrentUser.set(user); // Update singleton
                    }
                    Log.d(TAG, "Name updated successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating name", e);
                });
    }

    public void updateEmail(String email) {
        if (currentUserId == null) return;

        db.collection("users")
                .document(currentUserId)
                .update("email", email)
                .addOnSuccessListener(aVoid -> {
                    User user = userLiveData.getValue();
                    if (user != null) {
                        user.setEmail(email);
                        userLiveData.setValue(user);
                        CurrentUser.set(user); // Update singleton
                    }
                    Log.d(TAG, "Email updated successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating email", e);
                });
    }

    public void updatePhoneNumber(String phoneNumber) {
        if (currentUserId == null) return;

        db.collection("users")
                .document(currentUserId)
                .update("phoneNumber", phoneNumber)
                .addOnSuccessListener(aVoid -> {
                    User user = userLiveData.getValue();
                    if (user != null) {
                        user.setPhoneNumber(phoneNumber);
                        userLiveData.setValue(user);
                        CurrentUser.set(user); // Update singleton
                    }
                    Log.d(TAG, "Phone number updated successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating phone number", e);
                });
    }

    public LiveData<User> getUserData() {
        return userLiveData;
    }
}
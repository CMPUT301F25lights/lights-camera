package com.example.lotterize.ui.profile;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.lotterize.CurrentUser;
import com.example.lotterize.User;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * ViewModel class responsible for managing and updating the current user's profile data.
 * This class acts as a bridge between the Firestore database and the UI. It observes real-time updates from
 * Firestore and reflects any changes in the LiveData object that the UI listens to.
 * Additionally, it handles user data updates such as name, email, and phone number, ensuring
 * synchronization between Firestore, LiveData, and the CurrentUser singleton.
 */
public class ProfileViewModel extends ViewModel {

    private static final String TAG = "ProfileViewModel";

    private final MutableLiveData<User> userLiveData;

    private final FirebaseFirestore db;

    // The currently authenticated user's ID
    private String currentUserId;

    /**
     * Constructor initializes Firestore, retrieves the current user,
     * and sets up real-time data synchronization with Firestore.
     */
    public ProfileViewModel() {
        userLiveData = new MutableLiveData<>();
        db = FirebaseFirestore.getInstance();

        // Retrieve the logged-in user from the singleton
        User currentUser = CurrentUser.get();
        if (currentUser != null) {
            currentUserId = currentUser.getUserId();
            userLiveData.setValue(currentUser);
            loadUserData(); // Start observing changes in Firestore
        }
    }

    /**
     * Sets up a Firestore snapshot listener to automatically update LiveData
     * whenever the user's document changes.
     */
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
                        CurrentUser.set(user);
                    }
                });
    }

    /**
     * Updates the user's name both in Firestore and locally.
     *
     * @param name The new name to update.
     */
    public void updateName(String name) {
        if (currentUserId == null) return;

        db.collection("users")
                .document(currentUserId)
                .update("name", name)
                .addOnSuccessListener(aVoid -> {
                    // Update the LiveData and CurrentUser singleton
                    User user = userLiveData.getValue();
                    if (user != null) {
                        user.setName(name);
                        userLiveData.setValue(user);
                        CurrentUser.set(user);
                    }
                    Log.d(TAG, "Name updated successfully");
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error updating name", e)
                );
    }

    /**
     * Updates the user's email address both in Firestore and locally.
     *
     * @param email The new email address to update.
     */
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
                        CurrentUser.set(user);
                    }
                    Log.d(TAG, "Email updated successfully");
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error updating email", e)
                );
    }

    /**
     * Updates the user's phone number both in Firestore and locally.
     *
     * @param phoneNumber The new phone number to update.
     */
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
                        CurrentUser.set(user);
                    }
                    Log.d(TAG, "Phone number updated successfully");
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error updating phone number", e)
                );
    }

    /**
     * Permanently deletes the user's account from Firestore.
     * Also clears LiveData after deletion to update UI observers.
     */
    public void deleteAccount() {
        String userId = CurrentUser.get() != null ? CurrentUser.get().getUserId() : null;
        if (userId == null) return;

        db.collection("users")
                .document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User deleted successfully.");
                    userLiveData.setValue(null); // Notify UI that user is gone
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error deleting user: " + e.getMessage())
                );
    }

    /**
     * Exposes the LiveData object containing user information.
     *
     * @return A LiveData<User> that can be observed by Fragments or Activities.
     */
    public LiveData<User> getUserData() {
        return userLiveData;
    }
}

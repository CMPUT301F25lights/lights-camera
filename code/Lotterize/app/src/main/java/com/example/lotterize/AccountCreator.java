package com.example.lotterize;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

import androidx.annotation.NonNull;

/**
 * Handles the core logic of creating a new user account.
 * Separated from Android UI so it can be tested in JVM-only tests.
 */
public class AccountCreator {

    private final CollectionReference users;

    public interface Callback {
        void onSuccess(String message);
        void onFailure(String message);
    }

    public AccountCreator(CollectionReference users) {
        this.users = users;
    }

    public void createAccount(@NonNull String username, @NonNull String password, @NonNull Callback callback) {
        if (username.isEmpty() || password.isEmpty()) {
            callback.onFailure("Username and password cannot be empty");
            return;
        }

        users.whereEqualTo("username", username)
                .get(Source.SERVER)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        callback.onFailure("Username already exists");
                    } else {
                        User user = new User(username, password);
                        users.add(user)
                                .addOnSuccessListener(documentReference -> {
                                    String generatedId = documentReference.getId();
                                    documentReference.update("userId", generatedId);
                                    callback.onSuccess("Account created successfully!");
                                })
                                .addOnFailureListener(e ->
                                        callback.onFailure("Error creating account: " + e.getMessage())
                                );
                    }
                })
                .addOnFailureListener(e -> callback.onFailure("Error checking username: " + e.getMessage()));
    }
}

package com.example.lotterize;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lotterize.databinding.ActivityMainBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * {@code MainActivity} serves as the login screen for the Lotterize application.
 *
 * It allows users to sign in with an existing username and password or navigate
 * to the {@link CreateAccountActivity} to register a new account.
 * The activity uses Firebase Firestore to verify credentials and loads the
 * user's data into the {@link CurrentUser} singleton upon successful authentication.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseFirestore db;
    private CollectionReference users;
    private ActivityMainBinding binding;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button signInButton;
    private Button createAccountButton;
    private Button adminSignInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check for device linking BEFORE showing login screen
        if (checkAndAutoLogin()) {
            return; // Auto-login successful, activity will be finished
        }

        // Show normal login screen
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        users = db.collection("users");

        usernameEditText = findViewById(R.id.editTextUsername);
        passwordEditText = findViewById(R.id.editTextPassword);
        signInButton = findViewById(R.id.buttonSignin);
        createAccountButton = findViewById(R.id.buttonCreateAccount);
        adminSignInButton = findViewById(R.id.buttonAdminSignin);

        signInButton.setOnClickListener(v -> {
            String enteredUsername = usernameEditText.getText().toString().trim();
            String enteredPassword = passwordEditText.getText().toString().trim();

            if (enteredUsername.isEmpty() || enteredPassword.isEmpty()) {
                Toast.makeText(MainActivity.this, "Username and password cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            users.whereEqualTo("username", enteredUsername)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            boolean passwordMatch = false;
                            DocumentSnapshot[] successDoc = new DocumentSnapshot[1];

                            // Check password
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                String dbPassword = doc.getString("password");
                                if (enteredPassword.equals(dbPassword)) {
                                    passwordMatch = true;
                                    successDoc[0] = doc;
                                    break;
                                }
                            }

                            if (passwordMatch) {
                                loadUserAndNavigate(successDoc[0]);
                            } else {
                                Toast.makeText(MainActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(MainActivity.this, "Username not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(MainActivity.this, "Error checking username: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        createAccountButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CreateAccountActivity.class))
        );

        adminSignInButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AdminSignInActivity.class))
        );
    }

    /**
     * Checks if device linking is enabled and attempts auto-login.
     * @return true if auto-login was initiated, false otherwise
     */
    private boolean checkAndAutoLogin() {
        SharedPreferences prefs = getSharedPreferences("LotterizePrefs", MODE_PRIVATE);
        boolean deviceLinked = prefs.getBoolean("deviceLinked", false);
        String linkedUserId = prefs.getString("linkedUserId", null);

        if (deviceLinked && linkedUserId != null && !linkedUserId.isEmpty()) {
            Log.d(TAG, "Device linking enabled, attempting auto-login for user: " + linkedUserId);

            // Auto-login: Load user from Firestore
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(linkedUserId)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            Boolean deviceLinkedInDb = document.getBoolean("deviceLinked");
                            if (deviceLinkedInDb != null && deviceLinkedInDb) {
                                // User still has device linking enabled in Firestore
                                Log.d(TAG, "Auto-login successful");
                                loadUserAndNavigate(document);
                            } else {
                                // Device linking was disabled remotely
                                Log.d(TAG, "Device linking disabled remotely");
                                clearDeviceLinking();
                            }
                        } else {
                            // User document no longer exists
                            Log.d(TAG, "User document not found");
                            clearDeviceLinking();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error checking device linking", e);
                        // Continue to normal login on error
                    });

            return true;
        }

        return false;
    }

    /**
     * Loads user data from Firestore document and navigates to appropriate activity.
     * @param document The Firestore document containing user data
     */
    private void loadUserAndNavigate(DocumentSnapshot document) {
        // Create User object from Firestore data
        User user = new User(
                document.getId(),
                document.getString("name"),
                document.getString("phoneNumber"),
                document.getString("email"),
                document.getString("coordinates"),
                document.getString("username"),
                document.getString("password")
        );

        // Set notification preference
        Boolean wantNotification = document.getBoolean("wantNotification");
        user.setWantNotification(wantNotification == null || wantNotification);

        // Set device linking status
        Boolean deviceLinked = document.getBoolean("deviceLinked");
        user.setDeviceLinked(deviceLinked != null && deviceLinked);

        // Get Owned Event list from Firestore
        ArrayList<String> ownedEvents = (ArrayList<String>) document.get("ownedEventIds");
        if (ownedEvents == null) ownedEvents = new ArrayList<>();
        user.setOwnedEventIds(ownedEvents);

        // Get Registered Event list from Firestore
        ArrayList<String> registeredEvents = (ArrayList<String>) document.get("registeredEventIds");
        if (registeredEvents == null) registeredEvents = new ArrayList<>();
        user.setRegisteredEventIds(registeredEvents);

        // Set the logged-in user
        CurrentUser.set(user);

        // Navigate to appropriate activity
        startActivity(new Intent(MainActivity.this, UserActivity.class));
        finish(); // Close login screen
    }

    /**
     * Clears device linking data from SharedPreferences.
     */
    private void clearDeviceLinking() {
        SharedPreferences prefs = getSharedPreferences("LotterizePrefs", MODE_PRIVATE);
        prefs.edit()
                .putBoolean("deviceLinked", false)
                .remove("linkedUserId")
                .apply();
        Log.d(TAG, "Device linking cleared");
    }
}
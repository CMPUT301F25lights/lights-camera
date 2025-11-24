package com.example.lotterize;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

    private FirebaseFirestore db;
    private CollectionReference users;
    private ActivityMainBinding binding;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button signInButton;
    private Button createAccountButton;
    private Button adminSignInButton;

    /**
     * Called when the activity is first created.
     *
     * This method initializes Firestore, sets up view binding, connects UI elements,
     * and defines click listeners for both login and account creation actions.
     *
     * @param savedInstanceState If non-null, this activity is being re-created from a previous state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                            DocumentSnapshot[] successDoc = new DocumentSnapshot[1]; // mutable holder

                            // Checks password
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                String dbPassword = doc.getString("password");
                                if (enteredPassword.equals(dbPassword)) {
                                    passwordMatch = true;
                                    successDoc[0] = doc;
                                    break;
                                }
                            }

                            if (passwordMatch) {
                                // Load data into static instance CurrentUser
                                User user = new User(
                                        successDoc[0].getId(),
                                        successDoc[0].getString("name"),
                                        successDoc[0].getString("phoneNumber"),
                                        successDoc[0].getString("email"),
                                        successDoc[0].getString("coordinates"),
                                        successDoc[0].getString("username"),
                                        successDoc[0].getString("password")
                                );

                                user.setWantNotification(successDoc[0].getBoolean("wantNotification") == null || Boolean.TRUE.equals(successDoc[0].getBoolean("wantNotification")));

                                // Get Owned Event list from Firestore
                                ArrayList<String> ownedEvents =
                                        (ArrayList<String>) successDoc[0].get("ownedEventIds");
                                if (ownedEvents == null) ownedEvents = new ArrayList<>();
                                user.setOwnedEventIds(ownedEvents);

                                // Get Registered Event list from Firestore
                                ArrayList<String> registeredEvents =
                                        (ArrayList<String>) successDoc[0].get("registeredEventIds");
                                if (registeredEvents == null) registeredEvents = new ArrayList<>();
                                user.setRegisteredEventIds(registeredEvents);
                                CurrentUser.set(user); // set the logged-in user
                                startActivity(new Intent(MainActivity.this, UserActivity.class));


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

}
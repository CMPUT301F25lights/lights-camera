package com.example.lotterize;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.lotterize.databinding.ActivityCreateAccountBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText newUsernameEditText;
    private EditText newPasswordEditText;
    private Button createAccountButton;
    private Button backButton;

    private FirebaseFirestore db;
    private CollectionReference users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_account);

        newUsernameEditText = findViewById(R.id.editTextNewUsername);
        newPasswordEditText = findViewById(R.id.editTextNewPassword);
        createAccountButton = findViewById(R.id.buttonCreateNewAccount);
        backButton = findViewById(R.id.buttonBackFromCreateAccount);

        db = FirebaseFirestore.getInstance();
        users = db.collection("users");

        createAccountButton.setOnClickListener(v -> {
            String username = newUsernameEditText.getText().toString().trim();
            String password = newPasswordEditText.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username and password cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if username already exists
            users.whereEqualTo("username", username)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
                        } else {
                            // Create new user object
                            User user = new User(username, password);

                            users.add(user)
                                    .addOnSuccessListener(documentReference -> {

                                        String generatedId = documentReference.getId();
                                        documentReference.update("userId", generatedId);

                                        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                        // Go back to login screen to sign in
                                        finish();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "Error creating account: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error checking username: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        backButton.setOnClickListener(v -> finish()); // Back to sign in/create account
    }
}
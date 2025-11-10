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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class AdminSignInActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button signInButton;
    private Button backButton;

    private FirebaseFirestore db;
    private CollectionReference admins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.admin_login);

        usernameEditText = findViewById(R.id.editTextAdminUsername);
        passwordEditText = findViewById(R.id.editTextAdminPassword);
        signInButton = findViewById(R.id.buttonSignInAdmin);
        backButton = findViewById(R.id.buttonBackFromAdminLogin);

        db = FirebaseFirestore.getInstance();
        admins = db.collection("admins");

        signInButton.setOnClickListener(v -> {
            String enteredUsername = usernameEditText.getText().toString().trim();
            String enteredPassword = passwordEditText.getText().toString().trim();

            if (enteredUsername.isEmpty() || enteredPassword.isEmpty()) {
                Toast.makeText(AdminSignInActivity.this, "Username and password cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            admins.whereEqualTo("username", enteredUsername)
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

                                Intent intent = new Intent(AdminSignInActivity.this, AdminActivity.class);
                                intent.putExtra("adminUsername", enteredUsername); // pass the admin's username
                                startActivity(intent);

                            } else {

                                Toast.makeText(AdminSignInActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();

                            }

                        } else {
                            Toast.makeText(AdminSignInActivity.this, "Username not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(AdminSignInActivity.this, "Error checking username: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        backButton.setOnClickListener(v -> finish()); // Back to sign in/create account
    }
}
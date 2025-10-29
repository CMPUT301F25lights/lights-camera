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

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private CollectionReference users;
    private ActivityMainBinding binding;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button signInButton;
    private Button createAccountButton;


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
    }

}
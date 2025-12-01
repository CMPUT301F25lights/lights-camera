package com.example.lotterize;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class CreateAccountActivity extends AppCompatActivity {

    EditText newUsernameEditText;
    EditText newPasswordEditText;
    Button createAccountButton;
    private Button backButton;

    FirebaseFirestore db;
    CollectionReference users;
    AccountCreator accountCreator;

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
        accountCreator = new AccountCreator(users);

        createAccountButton.setOnClickListener(v -> {
            String username = newUsernameEditText.getText().toString().trim();
            String password = newPasswordEditText.getText().toString().trim();

            accountCreator.createAccount(username, password, new AccountCreator.Callback() {
                @Override
                public void onSuccess(String message) {
                    Toast.makeText(CreateAccountActivity.this, message, Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(CreateAccountActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });

        backButton.setOnClickListener(v -> finish());
    }
}

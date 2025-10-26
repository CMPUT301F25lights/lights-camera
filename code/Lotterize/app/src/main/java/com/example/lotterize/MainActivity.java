package com.example.lotterize;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.lotterize.databinding.ActivityMainBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private FirebaseFirestore db;
    private CollectionReference events;
    private Button userButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // uncomment code to custom add event to the data base
//        db = FirebaseFirestore.getInstance();
//        events = db.collection("events");
//        DocumentReference docRef = events.document("eventA");
//        ArrayList<String> waitList = new ArrayList<>();
//        String[] arr = {"a","b","c","d"};
//        waitList.addAll(Arrays.asList(arr));
//        docRef.set(new Event("a", "a", "a", "a", 2, 2, "a", 2, 2, waitList));

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userButton = findViewById(R.id.userButton);
        userButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, UserActivity.class);
                startActivity(intent);
            }
        } );
    }

}
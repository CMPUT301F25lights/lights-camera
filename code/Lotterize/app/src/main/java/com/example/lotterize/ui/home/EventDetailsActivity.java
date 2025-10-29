package com.example.lotterize.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lotterize.ActivityShowWaitingList;
import com.example.lotterize.databinding.ActivityEventDetailsBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;

public class EventDetailsActivity extends AppCompatActivity {

    FirebaseFirestore db;
    CollectionReference events;
    ActivityEventDetailsBinding binding;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        events = db.collection("events");

        binding = ActivityEventDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ImageButton back = binding.eventDetailsReturn;
        TextView header = binding.eventNameText;
        TextView date = binding.dateText;
        TextView time = binding.timeText;
        TextView location = binding.locationText;
        TextView entrants = binding.entrantsText;
        TextView description = binding.descText;

        Button waitListButton = binding.seeWaitList;
        waitListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventDetailsActivity.this, ActivityShowWaitingList.class);
                intent.putExtra("type", "waitList");
                intent.putExtra("eventId", getIntent().getLongExtra("eventId",0));
                startActivity(intent);
            }
        });

        events.whereEqualTo("eventId", getIntent().getLongExtra("eventId", 0)).limit(1).get().addOnSuccessListener(snapshot -> {
            if (!snapshot.isEmpty()) {
                DocumentSnapshot event = snapshot.getDocuments().get(0);
                header.setText(event.getString("eventName"));
                Date dateTime = event.getTimestamp("date").toDate();
                String timeString = String.format("%d:%02d",dateTime.getHours(),dateTime.getMinutes());
                time.setText(timeString);
                String dateString = dateTime.getDate() +"/" + String.valueOf(dateTime.getMonth()+1)+ "/" + String.valueOf(dateTime.getYear()+1900);
                date.setText(dateString);
                location.setText(event.getString("location"));
                List<Long> waitList = (List<Long>) event.get("waitList");
                String entrantsString = String.format("%d (%d Total Spots)", waitList.size(),event.getLong("totalSpots"));
                entrants.setText(entrantsString);
                description.setText(event.getString("description"));
            } else {
                Log.w("No event found:", getIntent().getStringExtra(("eventId")));
            }

        });



        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}

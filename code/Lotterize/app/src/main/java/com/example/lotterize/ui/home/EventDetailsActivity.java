package com.example.lotterize.ui.home;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lotterize.R;
import com.example.lotterize.databinding.ActivityEventDetailsBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.util.ArrayList;
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
                List waitList = event.get("waitList", List.class);
                String entrantsString = String.format("%d (%d Total Spots)", waitList.size(),event.getLong("totalSpots"));
                description.setText(entrantsString);
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

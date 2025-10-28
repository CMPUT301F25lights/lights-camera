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

public class EventDetailsActivity extends AppCompatActivity {

    FirebaseFirestore db;
    CollectionReference events;
    DocumentSnapshot event;
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
                event = snapshot.getDocuments().get(0);
                header.setText(event.getString("eventName"));
                time.setText((event.getString("time")));
                date.setText(event.getString("date"));
                location.setText(event.getString("location"));
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

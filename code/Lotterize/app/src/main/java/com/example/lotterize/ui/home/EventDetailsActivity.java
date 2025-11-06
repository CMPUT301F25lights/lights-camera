package com.example.lotterize.ui.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lotterize.QR;
import com.example.lotterize.databinding.ActivityEventDetailsBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
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
        TextView deadline = binding.deadlineText;
        TextView location = binding.locationText;
        TextView entrants = binding.entrantsText;
        TextView description = binding.descText;
        ImageView qrCode = binding.qrCodeImage;
        Button waitListButton = binding.seeWaitList;

        if (getIntent().getStringExtra("eventId") != null) {
            events.whereEqualTo("eventId", getIntent().getStringExtra("eventId")).limit(1).get().addOnSuccessListener(snapshot -> {
                if (!snapshot.isEmpty()) {
                    DocumentSnapshot event = snapshot.getDocuments().get(0);
                    header.setText(event.getString("eventName"));

                    Calendar dateTime = Calendar.getInstance();
                    if (event.getTimestamp("date") != null){
                        dateTime.setTime(event.getTimestamp("date").toDate());
                    } else {
                        Toast.makeText(this, "date couldn't be found - EventDetailsActivity", Toast.LENGTH_SHORT).show();
                    }

                    String amPm = dateTime.get(Calendar.AM_PM) == Calendar.AM ? "am" : "pm";
                    int hour = dateTime.get(Calendar.HOUR) == 0 ? 12 : dateTime.get(Calendar.HOUR);
                    @SuppressLint("DefaultLocale") String dateString = String.format("%d:%02d%s on %02d/%02d/%04d", hour, dateTime.get(Calendar.MINUTE), amPm,
                            dateTime.get(Calendar.DATE), dateTime.get(Calendar.MONTH)+1, dateTime.get(Calendar.YEAR));
                    date.setText(dateString);

                    if (event.getTimestamp("registrationDeadline") != null) {
                        dateTime.setTime(event.getTimestamp("registrationDeadline").toDate());
                    } else {
                        Toast.makeText(this, "deadline couldn't be found - EventDetailsActivity", Toast.LENGTH_SHORT).show();
                    }
                    amPm = dateTime.get(Calendar.AM_PM) == Calendar.AM ? "am" : "pm";
                    hour = dateTime.get(Calendar.HOUR) == 0 ? 12 : dateTime.get(Calendar.HOUR);
                    @SuppressLint("DefaultLocale") String deadlineString = String.format("%d:%02d%s on %02d/%02d/%04d", hour, dateTime.get(Calendar.MINUTE), amPm,
                            dateTime.get(Calendar.DATE), dateTime.get(Calendar.MONTH)+1, dateTime.get(Calendar.YEAR));
                    deadline.setText(deadlineString);

                    if (event.getString("location") != null) {
                        location.setText(event.getString("location"));
                    } else {
                        Toast.makeText(this, "location couldn't be found - EventDetailsActivity", Toast.LENGTH_SHORT).show();
                    }

                    if (event.get("waitList") != null){

                        waitListButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(EventDetailsActivity.this, ActivityShowWaitingList.class);
                                intent.putExtra("eventId", getIntent().getStringExtra("eventId"));
                                startActivity(intent);
                            }
                        });

                        List<Long> waitList = (List<Long>) event.get("waitList");
                        String entrantsString = String.format("%d (%d Total Spots)", waitList.size(),event.getLong("totalSpots"));
                        entrants.setText(entrantsString);
                    } else {
                        Toast.makeText(this, "waitList couldn't be found - EventDetailsActivity", Toast.LENGTH_SHORT).show();
                    }

                    if (event.getString("description") != null) {
                        description.setText(event.getString("description"));
                    } else {
                        Toast.makeText(this, "description couldn't be found - EventDetailsActivity", Toast.LENGTH_SHORT).show();
                    }

                    if (event.getString("qrCode") != null && !event.getString("qrCode").isEmpty()){
                        qrCode.setImageBitmap(QR.generateBitmap(event.getString("qrCode"), 100));
                    } else {
                        Toast.makeText(this, "qrCode couldn't be found - EventDetailsActivity", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(this, "Couldn't find event - EventDetailsActivity", Toast.LENGTH_SHORT).show();
                }

            });
        } else {
            Toast.makeText(this, "eventId is null - EventDetailsActivity", Toast.LENGTH_SHORT).show();
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}

package com.example.lotterize.ui.home;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.test.espresso.idling.CountingIdlingResource;

import com.example.lotterize.QR;
import com.example.lotterize.databinding.ActivityEventDetailsBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.List;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * Displays event details relevant to the user or admin.
 */
public class EventDetailsActivity extends AppCompatActivity {

    public static CountingIdlingResource showingEvent = new CountingIdlingResource("MakingEvents");
    FirebaseFirestore db;
    CollectionReference events;
    ActivityEventDetailsBinding binding;

    /**
     * Displays event details relevant to the user or admin. Expects intent to have eventId, and
     * optionally context (for admins)
     *
     * @param savedInstanceState intent contains a String in
     *                           "eventId" field
     */
    protected void onCreate(Bundle savedInstanceState) {
        showingEvent.increment();
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
        String context = getIntent().getStringExtra("context");
        if (context != null){
            if (getIntent().getStringExtra("context").equals("admin")){
                waitListButton.setText("Delete");
                waitListButton.setBackgroundColor(Color.RED);
            }
        }

        String eventId = getIntent().getStringExtra("eventId");

        if (eventId != null) {
            events.document(eventId).get().addOnSuccessListener(event -> {
                if (event.exists()) {
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

                    if (context != null && context.equals("admin")){
                        waitListButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new AlertDialog.Builder(EventDetailsActivity.this)
                                        .setTitle("Delete Event")
                                        .setMessage("Are you sure you want to delete this event? This will also remove it from all users' owned and registered events.")
                                        .setPositiveButton("Yes", (dialog, which) -> {
                                            db.collection("events").document(eventId)
                                                    .delete()
                                                    .addOnSuccessListener(unused -> {
                                                        // Clean up references in all users
                                                        db.collection("users")
                                                                .get()
                                                                .addOnSuccessListener(querySnapshot -> {
                                                                    for (QueryDocumentSnapshot userDoc : querySnapshot) {
                                                                        List<String> ownedEventIds = (List<String>) userDoc.get("ownedEventIds");
                                                                        List<String> registeredEventIds = (List<String>) userDoc.get("registeredEventIds");
                                                                        boolean changed = false;

                                                                        if (ownedEventIds != null && ownedEventIds.contains(eventId)) {
                                                                            ownedEventIds.remove(eventId);
                                                                            userDoc.getReference().update("ownedEventIds", ownedEventIds);
                                                                            changed = true;
                                                                        }

                                                                        if (registeredEventIds != null && registeredEventIds.contains(eventId)) {
                                                                            registeredEventIds.remove(eventId);
                                                                            userDoc.getReference().update("registeredEventIds", registeredEventIds);
                                                                            changed = true;
                                                                        }

                                                                        if (changed) {
                                                                            Log.d("AdminDeleteEvent", "Removed eventId from user: " + userDoc.getId());
                                                                        }
                                                                    }
                                                                })
                                                                .addOnFailureListener(e ->
                                                                        Log.e("AdminDeleteEvent", "Failed to clean up user references", e));

                                                        Toast.makeText(EventDetailsActivity.this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    })
                                                    .addOnFailureListener(e ->
                                                            Toast.makeText(EventDetailsActivity.this, "Failed to delete event", Toast.LENGTH_SHORT).show()
                                                    );
                                        })
                                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                        .show();
                            }
                        });
                    } else if (event.get("waitList") != null){

                        waitListButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(EventDetailsActivity.this, ShowWaitingListActivity.class);
                                intent.putExtra("eventId", eventId);
                                startActivity(intent);
                            }
                        });
                    } else {
                        Toast.makeText(this, "waitList couldn't be found or no valid context - EventDetailsActivity", Toast.LENGTH_SHORT).show();
                    }

                    if ( event.get("waitList") != null) {
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

                    ImageView posterImage = binding.eventPosterImage;
                    String imageUrl = event.getString("imageUrl");
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(this)
                                .load(imageUrl)
                                .into(posterImage);
                    } else {
                        posterImage.setVisibility(View.GONE);
                    }
                    showingEvent.decrement();

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

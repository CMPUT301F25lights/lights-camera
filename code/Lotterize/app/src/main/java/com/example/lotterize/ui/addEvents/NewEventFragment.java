package com.example.lotterize.ui.addEvents;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotterize.CurrentUser;
import com.example.lotterize.Event;
import com.example.lotterize.QR;
import com.example.lotterize.R;
import com.example.lotterize.User;
import com.example.lotterize.databinding.FragmentNewEventBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.net.Uri;
import android.util.Log;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class NewEventFragment extends Fragment {
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private CollectionReference events;
    private FragmentNewEventBinding binding;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private Uri ImageUri;
    private String imageUrl = null;
    private TextView imageSelectedTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference().child("event_posters");

        // Initialize photo picker
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {// image selected
                Log.d("PhotoPicker", "Selected URI: " + uri);
                // store selected image uri
                ImageUri = uri;
                // update textview to show image selected
                imageSelectedTextView.setText(uri.toString());
                imageSelectedTextView.setVisibility(View.VISIBLE);
                Log.d("Upload", "=== STARTING UPLOAD ===");
                Log.d("Upload", "URI: " + ImageUri);

                StorageReference imageRef = storageRef.child( "TestImage_" + System.currentTimeMillis() + ".jpg");
                Log.d("Upload", "Storage path: " + imageRef.getPath());

                UploadTask uploadTask = imageRef.putFile(ImageUri);

                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    Log.d("Upload", "=== UPLOAD SUCCESS ===");
                    imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        this.imageUrl = downloadUri.toString();
                        Log.d("Upload", "Got download URL: " + this.imageUrl);
                        Toast.makeText(getContext(), "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Log.e("Upload", "Failed to get download URL", e);
                        this.imageUrl = null;
                    });
                }).addOnFailureListener(e -> {
                    Log.e("Upload", "=== UPLOAD FAILED ===", e);
                    Toast.makeText(getContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

            } else {
                // no image selected
                Log.d("PhotoPicker", "No media selected");
            }
        });
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NewEventViewModel newEventViewModel =
                new ViewModelProvider(this).get(NewEventViewModel.class);

        binding = FragmentNewEventBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();
        events = db.collection("events");

        // selected image textview
        imageSelectedTextView = binding.imageSelectedTextView;
        imageSelectedTextView.setVisibility(View.GONE);

        // select image button
        binding.buttonSelectImage.setOnClickListener(v -> {
            // launch photo picker
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        binding.buttonCreateEvent.setOnClickListener(v -> {

            // Collect user inputs
            String eventName = binding.eventNameInput.getText().toString().trim();
            String dateString = binding.dateInput.getText().toString().trim();
            String timeString = binding.timeInput.getText().toString().trim();
            String registrationStartString = binding.registrationStartInput.getText().toString().trim();
            String registrationEndString = binding.registrationEndInput.getText().toString().trim();
            Timestamp date; //-------------------------------
            Timestamp registrationStartDate; //-------------------------------
            Timestamp registrationEndDate; //-------------------------------
            String location = binding.locationInput.getText().toString().trim(); //-------------------------------
            String totalSpotsString = binding.totalSpotsInput.getText().toString().trim();
            Long totalSpots = Long.parseLong(totalSpotsString); //-------------------------------
            String description = binding.descriptionInput.getText().toString().trim(); //-------------------------------
            String entrantsLimitString = binding.entrantsLimitInput.getText().toString().trim();
            Long entrantsLimit = Long.parseLong(entrantsLimitString); //-------------------------------
            String qrCode = QR.generateCode();

            // Required field check
            if (eventName.isEmpty() || dateString.isEmpty() || timeString.isEmpty() ||
                    registrationStartString.isEmpty() || registrationEndString.isEmpty() ||
                    location.isEmpty() || totalSpotsString.isEmpty() || entrantsLimitString.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all required fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate number inputs
            try {
                if (totalSpots <= 0 || entrantsLimit <= 0) {
                    Toast.makeText(getContext(), "Spots and limit must be greater than 0!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (entrantsLimit > totalSpots) {
                    Toast.makeText(getContext(), "Entrants limit cannot exceed total spots!", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid number format!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate and parse dates
            Timestamp eventDate, regStartDate, regEndDate;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date eventDateParsed = sdf.parse(dateString + " " + timeString);
                Date regStartParsed = sdf.parse(registrationStartString + " 00:00");
                Date regEndParsed = sdf.parse(registrationEndString + " 23:59");

                if (eventDateParsed == null || regStartParsed == null || regEndParsed == null) {
                    Toast.makeText(getContext(), "Invalid date format!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate logical order
                if (!regStartParsed.before(regEndParsed)) {
                    Toast.makeText(getContext(), "Registration start must be before end!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!regEndParsed.before(eventDateParsed)) {
                    Toast.makeText(getContext(), "Registration must close before event date!", Toast.LENGTH_SHORT).show();
                    return;
                }

                eventDate = new Timestamp(eventDateParsed);
                regStartDate = new Timestamp(regStartParsed);
                regEndDate = new Timestamp(regEndParsed);

            } catch (Exception e) {
                Toast.makeText(getContext(), "Incorrect date format, use YYYY-MM-DD", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get ID of logged-in user
            User currentUser = CurrentUser.get();
            String ownerId = currentUser.getUserId();

            if (ownerId == null) {
                Toast.makeText(getContext(), "User not logged in!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Initialize participant lists
            ArrayList<String> waitList = new ArrayList<>();
            ArrayList<String> selectedList = new ArrayList<>();
            ArrayList<String> cancelledList = new ArrayList<>();
            ArrayList<String> finalList = new ArrayList<>();

            // Create Event object without ID
            Event event = new Event(null, ownerId, waitList, selectedList, cancelledList, finalList,
                    eventName, eventDate, regStartDate, regEndDate, location,
                    totalSpots, description, entrantsLimit, qrCode, imageUrl);

            // Save to Firestore
            events.add(event).addOnSuccessListener(documentReference -> {
                String eventId = documentReference.getId();

                currentUser.addOwnedEvent(eventId);
                db.collection("users")
                        .document(ownerId)
                        .update("ownedEventIds", currentUser.getOwnedEventIds())
                        .addOnSuccessListener(unused ->
                                Toast.makeText(getContext(), "Event created successfully!", Toast.LENGTH_SHORT).show()
                        )
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Failed to update user events!", Toast.LENGTH_SHORT).show()
                        );

                // Get event id
                documentReference.update("eventId", eventId)
                        .addOnSuccessListener(unused ->
                                Toast.makeText(getContext(), "Event created successfully!", Toast.LENGTH_SHORT).show()
                        );

                NavHostFragment.findNavController(NewEventFragment.this)
                        .navigate(R.id.navigation_addEvents);

            }).addOnFailureListener(e ->
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        });

        binding.buttonCancelCreateEvent.setOnClickListener(v ->
                NavHostFragment.findNavController(NewEventFragment.this)
                        .navigate(R.id.navigation_addEvents)
        );

        return root;
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
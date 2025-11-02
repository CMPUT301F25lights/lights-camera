package com.example.lotterize.ui.addEvents;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotterize.Event;
import com.example.lotterize.R;
import com.example.lotterize.databinding.FragmentNewEventBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class NewEventFragment extends Fragment {
    private FirebaseFirestore db;
    private CollectionReference events;
    private FragmentNewEventBinding binding;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private Uri ImageUri;
    private TextView imageSelectedTextView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NewEventViewModel newEventViewModel =
                new ViewModelProvider(this).get(NewEventViewModel.class);

        binding = FragmentNewEventBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();
        events = db.collection("events");

        imageSelectedTextView = binding.imageSelectedTextView;
        imageSelectedTextView.setVisibility(View.GONE);

        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {// image selected
                Log.d("PhotoPicker", "Selected URI: " + uri);
                // store selected image uri
                ImageUri = uri;
                // update textview to show image selected
                imageSelectedTextView.setText(uri.toString());
                imageSelectedTextView.setVisibility(View.VISIBLE);
            } else {
                // no image selected
                Log.d("PhotoPicker", "No media selected");
            }
        });
        binding.buttonSelectImage.setOnClickListener(v -> {
            // launch photo picker
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        binding.buttonCreateEvent.setOnClickListener(v -> {

            Long eventId = 101L; //-------------------------------
            Long ownerId = 6L; //-------------------------------
            ArrayList<Long> waitList = new ArrayList<>(); //-------------------------------
            ArrayList<Long> selectedList = new ArrayList<>(); //-------------------------------
            ArrayList<Long> cancelledList = new ArrayList<>(); //-------------------------------
            ArrayList<Long> finalList = new ArrayList<>(); //-------------------------------

            waitList.add(111L);
            waitList.add(222L);
            selectedList.add(333L);
            selectedList.add(444L);
            selectedList.add(555L);
            cancelledList.add(555L);
            finalList.add(333L);
            finalList.add(444L);

            String eventName = binding.eventNameInput.getText().toString().trim(); //-------------------------------
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
            String qrCode = binding.qrCodeInput.getText().toString().trim();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            try {
                Date date_date = sdf.parse(dateString + " " + timeString);
                Date date_regStartDate = sdf.parse(registrationStartString);
                Date date_regEndDate = sdf.parse(registrationEndString);
                date = new Timestamp(date_date);
                registrationStartDate = new Timestamp(date_regStartDate);
                registrationEndDate = new Timestamp(date_regEndDate);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            Event event = new Event(eventId, ownerId, waitList, selectedList, cancelledList, finalList,
                            eventName, date, registrationStartDate, registrationEndDate, location,
                            totalSpots, description, entrantsLimit, qrCode);


            db.collection("events")
                    .add(event)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(getContext(), "Event created!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

            NavHostFragment.findNavController(NewEventFragment.this)
                    .navigate(R.id.navigation_addEvents);
        });

        binding.buttonCancelCreateEvent.setOnClickListener(v -> {
            NavHostFragment.findNavController(NewEventFragment.this)
                    .navigate(R.id.navigation_addEvents);
        });

        return root;
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
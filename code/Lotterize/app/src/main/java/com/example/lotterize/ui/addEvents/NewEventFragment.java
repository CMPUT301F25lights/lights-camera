package com.example.lotterize.ui.addEvents;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotterize.CurrentUser;
import com.example.lotterize.Event;
import com.example.lotterize.ImageHandler;
import com.example.lotterize.R;
import com.example.lotterize.User;
import com.example.lotterize.databinding.FragmentNewEventBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.firebase.firestore.GeoPoint;


/**
 * {@code NewEventFragment} handles the creation of new events within the Lotterize app.
 * This fragment provides a user interface for event organizers to input event details,
 * select an optional image, validate input fields, and submit the event to Firebase Firestore.
 * It also updates the current user's owned event list upon successful creation.
 * The fragment uses the {@link FragmentNewEventBinding} class for view binding,
 * and integrates with {@link FirebaseFirestore} for persistent data storage.
 */
public class NewEventFragment extends Fragment implements ChooseEventFiltersFragment.ChooseEventFiltersDialogListener {
    private FirebaseFirestore db;
    private ImageHandler imageHandler;
    private CollectionReference events;
    private FragmentNewEventBinding binding;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private TextView imageSelectedTextView;

    private ArrayList<String> filtersList ;

    /**
     * Initializes the fragment and sets up the photo picker for event images.
     * Handles image selection, uploading to Firebase Storage, and storing the download URL.
     *
     * @param savedInstanceState If non-null, this fragment is being re-created from a previous state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filtersList = new ArrayList<>();

        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                binding.imageSelectedTextView.setText(uri.toString());
                binding.imageSelectedTextView.setVisibility(View.VISIBLE);
                imageHandler.addImage(getContext(), uri,
                        () -> {
                            // onSuccess
                        },
                        () -> {
                            // onFailure
                        }
                );

            }
        });
    }

    /**
     * Called when the fragment is first created.
     *
     * @param savedInstanceState If non-null, this fragment is being re-created from a previous state.
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NewEventViewModel newEventViewModel =
                new ViewModelProvider(this).get(NewEventViewModel.class);

        binding = FragmentNewEventBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();
        events = db.collection("events");

        imageHandler = new ImageHandler();

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

        // remove image button
        binding.buttonRemoveImage.setOnClickListener(v -> {
            imageHandler.removeImage(getContext(),null);
            // Reset UI
            imageSelectedTextView.setText("");
            imageSelectedTextView.setVisibility(View.GONE);
        });

        // Geolocation switch
        Switch geoSwitch = binding.switchGeolocation;

        // Dates and times
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH);
        int dayOfMonth = now.get(Calendar.DAY_OF_MONTH);

        EditText dateText = binding.dateText;
        Button dateButton = binding.dateInputButton;
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePicker = new DatePickerDialog(
                        getContext(),
                        (view, y, m, d) -> {
                            String dateString = String.format("%04d-%02d-%02d", y, (m + 1), d);
                            dateText.setText(dateString);
                        },
                        year,
                        month,
                        dayOfMonth
                );
                datePicker.show();
            }
        });

        EditText timeText = binding.timeText;
        Button timeButton = binding.timeInputButton; // timeString
        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePicker = new TimePickerDialog(
                        getContext(),
                        (view, hourOfDay, minute) -> {
                            String timeString = String.format("%02d:%02d", hourOfDay, minute);
                            timeText.setText(timeString);
                        },
                        12,
                        0,
                        true    // 24-hour mode; set false for AM/PM
                );
                timePicker.show();
            }
        });

        EditText regStartDateText = binding.registrationStartDateText;
        Button regStartDateButton = binding.registrationStartDateButton;
        regStartDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePicker = new DatePickerDialog(
                        getContext(),
                        (view, y, m, d) -> {
                            String dateString = String.format("%04d-%02d-%02d", y, (m + 1), d);
                            regStartDateText.setText(dateString);
                        },
                        year,
                        month,
                        dayOfMonth
                );
                datePicker.show();
            }
        });

        EditText regEndDateText = binding.registrationEndDateText;
        Button regEndDateButton = binding.registrationEndDateButton;
        regEndDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePicker = new DatePickerDialog(
                        getContext(),
                        (view, y, m, d) -> {
                            String dateString = String.format("%04d-%02d-%02d", y, (m + 1), d);
                            regEndDateText.setText(dateString);
                        },
                        year,
                        month,
                        dayOfMonth
                );
                datePicker.show();
            }
        });

        // Set Filters
        Button filterButton = binding.addEventFiltersButton;
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChooseEventFiltersFragment frag = new ChooseEventFiltersFragment();
                Bundle args = new Bundle();
                args.putSerializable("Current Filters", filtersList);
                frag.setArguments(args);
                frag.setListener(NewEventFragment.this);
                frag.show(getActivity().getSupportFragmentManager(), "Filters");
            }
        });

        binding.buttonCreateEvent.setOnClickListener(v -> {

            // wait until image is uploaded
            if (imageHandler.isUploading()) {
                Toast.makeText(getContext(), "Please wait: image is uploading", Toast.LENGTH_SHORT).show();
                return;
            }

            // Collect user inputs
            String eventName = binding.eventNameInput.getText().toString().trim();
            String dateString = binding.dateText.getText().toString().trim();
            String timeString = binding.timeText.getText().toString().trim();
            String registrationStartString = binding.registrationStartDateText.getText().toString().trim();
            String registrationEndString = binding.registrationEndDateText.getText().toString().trim();
            Timestamp date; //-------------------------------
            Timestamp registrationStartDate; //-------------------------------
            Timestamp registrationEndDate; //-------------------------------
            String location = binding.locationInput.getText().toString().trim(); //-------------------------------
            String totalSpotsString = binding.totalSpotsInput.getText().toString().trim();
            String description = binding.descriptionInput.getText().toString().trim(); //-------------------------------
            String entrantsLimitString = binding.entrantsLimitInput.getText().toString().trim();
            //String qrCode = QR.generateCode(); deprecated, use eventId instead
            Boolean geolocationEnabled = binding.switchGeolocation.isChecked();

            // Required field check
            if (eventName.isEmpty() || dateString.isEmpty() || timeString.isEmpty() ||
                    registrationStartString.isEmpty() || registrationEndString.isEmpty() ||
                    location.isEmpty() || totalSpotsString.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all required fields!", Toast.LENGTH_SHORT).show();
                return;
            }
            Long totalSpots = Long.parseLong(totalSpotsString); //-------------------------------
            Long entrantsLimit = !entrantsLimitString.equals("") ? Long.parseLong(entrantsLimitString) : 0; //-------------------------------

            // Validate number inputs
            try {
                if (totalSpots <= 0 || entrantsLimit < 0) {
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
            HashMap<String, GeoPoint> userLocations = new HashMap<>();

            // Add filters to Firestore

            for (String filterName : filtersList) {

                if (filterName == null || filterName.trim().isEmpty()) continue;

                String cleanName = filterName.trim();

                // Query for existing documents with the same name
                db.collection("filters")
                        .whereEqualTo("name", cleanName)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (querySnapshot.isEmpty()) {
                                // No doc exists, add a new one
                                HashMap<String, Object> data = new HashMap<>();
                                data.put("name", cleanName);

                                db.collection("filters")
                                        .add(data)
                                        .addOnSuccessListener(docRef -> {
                                            // success
                                        });
                            }
                        });
            }

            // Create Event object without ID
            Event event = new Event(null, ownerId, waitList, selectedList, cancelledList, finalList,
                    eventName, eventDate, regStartDate, regEndDate, location,
                    totalSpots, description, entrantsLimit, null, imageHandler.getUploadedImageUrl(), imageHandler.getUploadedImagePath(), filtersList,
                    geolocationEnabled, userLocations);

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

                // make qrCode same as eventId
                documentReference.update("qrCode", eventId)
                        .addOnSuccessListener(unused ->
                                Toast.makeText(getContext(), "Event created successfully!", Toast.LENGTH_SHORT).show()
                        );

                NavHostFragment.findNavController(NewEventFragment.this)
                        .navigate(R.id.navigation_addEvents);

            }).addOnFailureListener(e ->
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        });

        binding.buttonCancelCreateEvent.setOnClickListener(v ->{
            imageHandler.removeImage(getContext(),null);
            NavHostFragment.findNavController(NewEventFragment.this)
                    .navigate(R.id.navigation_addEvents);
        });

        return root;
    }

    @Override
    public void addFilter(String f) {
        filtersList.add(f);
    }

    @Override
    public void removeFilter(String f) {
        filtersList.remove(f);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
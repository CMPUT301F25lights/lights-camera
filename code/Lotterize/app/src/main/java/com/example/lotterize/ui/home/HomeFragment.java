package com.example.lotterize.ui.home;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lotterize.EventScheduler;


import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.test.espresso.idling.CountingIdlingResource;

import com.example.lotterize.CurrentUser;
import com.example.lotterize.R;
import com.example.lotterize.UserActivity;
import com.example.lotterize.databinding.FragmentHomeBinding;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import kotlin._Assertions;


/**
 * Displays events that user can join waitlist for along with text search,
 * QR search, and info about the lottery. User can also filter events using filters and
 * availability date.
 */
public class HomeFragment extends Fragment implements FilterFragment.FilterFragmentsDialogListener {
    private FirebaseFirestore db;
    private CollectionReference events;
    private FragmentHomeBinding binding;
    private ArrayList<DocumentSnapshot> eventList;
    private ArrayList<String> filtersList;
    private Calendar shownDate;
    private ArrayList<DocumentSnapshot> shownList;
    private ImageButton info;
    private ListView eventsListView;
    private ArrayAdapter<DocumentSnapshot> shownListAdapter;
    private EventScheduler scheduler;
    private ImageButton qrButton;
    public static CountingIdlingResource loadingEvents = new CountingIdlingResource("MakingEvents");
    private final ActivityResultLauncher<ScanOptions> qrLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    String qrValue = result.getContents();
                    Toast.makeText(getContext(), "QR: " + qrValue, Toast.LENGTH_SHORT).show();
                    openEventFromQR(qrValue);
                }
            });


    /**
     * Creates the home view
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return View
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    /**
     * Displays events that user can join waitlist for along with text search,
     * QR search, and info about the lottery. User can also filter events using filters and
     * availability date.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    public void onViewCreated (@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eventList = new ArrayList<>();
        shownList = new ArrayList<>();
        shownListAdapter = new EventListArrayAdapter(requireContext(), shownList);
        db = FirebaseFirestore.getInstance();
        events = db.collection("events");
        filtersList = new ArrayList<>();
        shownDate = null;

        scheduler = new EventScheduler();


        events.orderBy("registrationDeadline").whereGreaterThan("registrationDeadline", Timestamp.now())
                .addSnapshotListener((snapshot,e) -> {
                    if (e != null){
                        Toast.makeText(getContext(), "couldn't update data - HomeFragment", Toast.LENGTH_SHORT).show();
                    } else {
                        if (snapshot != null){
                            filtersList = new ArrayList<>();
                            shownDate = null;
                            eventList.clear();
                            shownList.clear();
                            for (DocumentSnapshot d : snapshot) {
                                Timestamp t = d.getTimestamp("registrationStart");
                                String ownerId = d.getString("ownerId");
                                if (t != null && ownerId != null && t.compareTo(Timestamp.now()) < 0 && !ownerId.equals(CurrentUser.get().getUserId())){
                                    eventList.add(d);
                                    shownList.add(d);
                                }
                            }
                            shownListAdapter.notifyDataSetChanged();
                        }
                    }
                });

        info = binding.infoButton;
        qrButton = binding.QRScanButton;
        eventsListView = binding.eventsList;
        TextInputEditText searchBar = binding.searchBar;

        eventsListView.setAdapter(shownListAdapter);

        MaterialButton waitListedEvents = binding.waitlistedEventsButton;
        waitListedEvents.setBackgroundTintList(new ColorStateList( new int[][]{new int[]{android.R.attr.state_checked},
                new int[]{}}, new int[]{Color.GRAY, Color.WHITE}));
        MaterialButton filterEvents = binding.filterEventsButton;
        String currUserId = CurrentUser.get().getUserId();
        waitListedEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());
                if (!v.isSelected()) {
                    shownList.clear();
                    updateShownList();
                } else {
                    shownList.clear();
                    for (DocumentSnapshot d : eventList){
                        List<String> l = (List<String>) d.get("waitList");
                        if (l != null && l.contains(currUserId)){
                            shownList.add(d);
                        }
                    }
                    shownListAdapter.notifyDataSetChanged();
                }
            }
        });

        filterEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterFragment filterFragment = new FilterFragment();
                Bundle args = new Bundle();
                args.putSerializable("Current Filters", filtersList);
                args.putSerializable("date", shownDate);
                filterFragment.setArguments(args);
                filterFragment.setListener(HomeFragment.this);
                filterFragment.show(getActivity().getSupportFragmentManager(), "Filter");
            }
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                shownList.clear();
                shownListAdapter.notifyDataSetChanged();
                String search = s.toString().toLowerCase();
                if (search.isEmpty()){
                    updateShownList();
                } else {
                    for (DocumentSnapshot d : eventList){
                        if (d.getString("eventName") != null && d.getString("eventName").toLowerCase().contains(search)){
                            shownList.add(d);
                            shownListAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });


        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InfoFragment i = new InfoFragment();
                i.show(getActivity().getSupportFragmentManager(), "Info");
            }
        });
        scheduler.monitorEvents();

        qrButton.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Scan the event QR code");
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
            options.setCameraId(0);  // back camera
            options.setBeepEnabled(true);
            options.setOrientationLocked(true);

            qrLauncher.launch(options);
        });

    }
    
    private void openEventFromQR(String eventId){
        try{
            DocumentReference ref = db.collection("events").document(eventId);
            ref.get().addOnSuccessListener(doc -> {
                if (!doc.exists()) {
                    Toast.makeText(getContext(), "Event does not exist.", Toast.LENGTH_LONG).show();
                    return;
                }

                Timestamp registrationStart = doc.getTimestamp("registrationStart");
                Timestamp registrationDeadline = doc.getTimestamp("registrationDeadline");
                String ownerId = doc.getString("ownerId");
                Timestamp now = Timestamp.now();
                boolean qrIsValid = true;
                String validationMessage = "";

                if (registrationStart == null || registrationStart.compareTo(now) > 0) {
                    qrIsValid = false;
                    validationMessage = "Registration has not started yet.";
                } else if (registrationDeadline == null || registrationDeadline.compareTo(now) < 0) {
                    qrIsValid = false;
                    validationMessage = "Registration for this event has closed.";
                }else if (ownerId != null && ownerId.equals(CurrentUser.get().getUserId())) {
                    qrIsValid = false;
                    validationMessage = "You cannot join the waitlist for your own event.";
                }

                if(qrIsValid){
                    Intent intent = new Intent(requireContext(), EventDetailsActivity.class);
                    intent.putExtra("eventId", eventId);
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), validationMessage, Toast.LENGTH_LONG).show();
                }
            });
        }catch (Exception e) {
            Toast.makeText(getContext(), "Event does not exist", Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Adds filter to filtersList
     * @param f
     */
    @Override
    public void addFilter(String f) {
        filtersList.add(f);
        updateShownList();
    }

    /**
     * Removes filter to filtersList
     * @param f
     */
    @Override
    public void removeFilter(String f) {
        filtersList.remove(f);
        updateShownList();
    }

    /**
     * Sets the shownDate
     * @param year
     * @param month
     * @param dayOfMonth
     */
    @Override
    public void filterDate(int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        shownDate = c;
        updateShownList();
    }

    /**
     * Resets the shownDate (makes it null)
     */
    @Override
    public void resetDate() {
        shownDate = null;
        updateShownList();
    }

    /**
     * Updates shownList taking into account filters and shownDate
     */
    private void updateShownList(){
        shownList.clear();
        for (DocumentSnapshot d : eventList){
            boolean containsAny = false;
            boolean sameDay = false;
            if (!filtersList.isEmpty()) {
                if (d.get("filtersList") != null) {
                    List<String> eventsFilters = (List<String>) d.get("filtersList");
                    for (String filter : filtersList) {
                        if (eventsFilters.contains(filter)) {
                            containsAny = true;
                            break;
                        }
                    }
                }
            } else {
                containsAny = true;
            }

            if (shownDate != null) {
                Timestamp eventDate = d.getTimestamp("date");
                if (eventDate != null) {
                    Calendar c = Calendar.getInstance();
                    c.setTime(eventDate.toDate());
                    if (c.get(Calendar.YEAR) == shownDate.get(Calendar.YEAR) &&
                            c.get(Calendar.MONTH) == shownDate.get(Calendar.MONTH) &&
                            c.get(Calendar.DAY_OF_MONTH) == shownDate.get(Calendar.DAY_OF_MONTH)) {
                        sameDay = true;
                    }
                }
            } else {
                sameDay = true;
            }

            if (containsAny && sameDay) {
                shownList.add(d);
            }
        }
        shownListAdapter.notifyDataSetChanged();
    }



    /**
     * Destroys the view
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
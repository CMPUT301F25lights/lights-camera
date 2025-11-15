package com.example.lotterize.ui.home;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lotterize.EventScheduler;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.lotterize.CurrentUser;
import com.example.lotterize.R;
import com.example.lotterize.databinding.FragmentHomeBinding;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;


/**
 * Fragment that shows the home screen. Includes a display of events
 * that user can join waitlist for along with text search, QR search (not implemented yet),
 * and info about the lottery.
 */
public class HomeFragment extends Fragment {
    private FirebaseFirestore db;
    private CollectionReference events;
    private FragmentHomeBinding binding;
    private ArrayList<DocumentSnapshot> eventList;

    private ArrayList<DocumentSnapshot> shownList;
    private ImageButton info;
    private ListView eventsListView;
    private ArrayAdapter<DocumentSnapshot> eventsArray;
    private ImageButton qrCodeButton;
    private EventScheduler scheduler;

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
     * QR search (not implemented yet), and info about the lottery.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    public void onViewCreated (@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eventList = new ArrayList<>();
        shownList = new ArrayList<>();
        eventsArray = new EventListArrayAdapter(requireContext(), shownList);
        db = FirebaseFirestore.getInstance();
        events = db.collection("events");

        scheduler = new EventScheduler();


        events.orderBy("registrationDeadline").whereGreaterThan("registrationDeadline", Timestamp.now())
                .addSnapshotListener((snapshot,e) -> {
                    if (e != null){
                        Toast.makeText(getContext(), "couldn't update data - HomeFragment", Toast.LENGTH_SHORT).show();
                    } else {
                        if (snapshot != null){
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
                            eventsArray.notifyDataSetChanged();
                        }
                    }
                });

        info = binding.infoButton;
        eventsListView = binding.eventsList;
        qrCodeButton = binding.QRScanButton;
        TextInputEditText searchBar = binding.searchBar;

        eventsListView.setAdapter(eventsArray);

        MaterialButton waitListedEvents = binding.waitlistedEventsButton;
        MaterialButton filterEvents = binding.filterEventsButton;
        String currUserId = CurrentUser.get().getUserId();
        waitListedEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());
                if (!v.isSelected()) {
                    shownList.clear();
                    shownList.addAll(eventList);
                    eventsArray.notifyDataSetChanged();
                } else {
                    shownList.clear();
                    for (DocumentSnapshot d : eventList){
                        List<String> l = (List<String>) d.get("waitList");
                        if (l != null && l.contains(currUserId)){
                            shownList.add(d);
                        }
                    }
                    eventsArray.notifyDataSetChanged();
                }
            }
        });

        filterEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());
                if (!v.isSelected()) {
                    shownList.clear();
                    shownList.addAll(eventList);
                    eventsArray.notifyDataSetChanged();
                } else {
                    // Filter here once data set changed

                    for (DocumentSnapshot d : eventList){
                        List<String> l = (List<String>) d.get("filters");
                        if (l != null && l.contains(currUserId)){
                            shownList.add(d);
                        }
                    }
                    eventsArray.notifyDataSetChanged();
                }
            }
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                shownList.clear();
                eventsArray.notifyDataSetChanged();
                String search = s.toString().toLowerCase();
                if (search.isEmpty()){
                    shownList.addAll(eventList);
                } else {
                    for (DocumentSnapshot d : eventList){
                        if (d.getString("eventName") != null && d.getString("eventName").toLowerCase().contains(search)){
                            shownList.add(d);
                            eventsArray.notifyDataSetChanged();
                        }
                    }
                }
            }
        });


        qrCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "FORNITE BATTLEPASS", Toast.LENGTH_SHORT).show();
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
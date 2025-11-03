package com.example.lotterize.ui.home;

import android.content.Intent;
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

public class HomeFragment extends Fragment {
    private FirebaseFirestore db;
    private CollectionReference events;
    private FragmentHomeBinding binding;
    private ArrayList<DocumentSnapshot> eventList;

    private ArrayList<DocumentSnapshot> shownList;
    private ImageButton info;
    private ListView eventsListView;
    private ArrayAdapter<DocumentSnapshot> eventsArray;
    private TextView info_text;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    public void onViewCreated (@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eventList = new ArrayList<>();
        shownList = new ArrayList<>();
        eventsArray = new EventListArrayAdapter(requireContext(), shownList);
        db = FirebaseFirestore.getInstance();
        events = db.collection("events");

        events.orderBy("registrationDeadline").whereGreaterThan("registrationDeadline", Timestamp.now())
                .get().addOnSuccessListener(snapshot -> {
            eventList.addAll(snapshot.getDocuments());
            shownList.addAll(eventList);
            eventsArray.notifyDataSetChanged();
        });

        events.orderBy("registrationDeadline").whereGreaterThan("registrationDeadline", Timestamp.now())
                .addSnapshotListener((snapshot,e) -> {
                    if (e != null){
                        Toast.makeText(getContext(), "couldn't update data - HomeFragment", Toast.LENGTH_SHORT).show();
                    } else {
                        eventList.clear();
                        eventList.addAll(snapshot.getDocuments());
                        shownList.clear();
                        shownList.addAll(eventList);
                        eventsArray.notifyDataSetChanged();
                    }

                    shownList.addAll(eventList);
                });

        info = binding.infoButton;
        eventsListView = binding.eventsList;
        TextInputEditText searchBar = binding.searchBar;

        eventsListView.setAdapter(eventsArray);

        MaterialButton waitListedEvents = binding.waitlistedEventsButton;
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

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InfoFragment i = new InfoFragment();
                i.show(getActivity().getSupportFragmentManager(), "Info");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
package com.example.lotterize.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.lotterize.Event;
import com.example.lotterize.databinding.FragmentHomeBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {
    private FirebaseFirestore db;
    private CollectionReference events;
    private FragmentHomeBinding binding;
    private ArrayList<Event> eventList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();
        events = db.collection("events");

        eventList = new ArrayList<>();
//        events.addSnapshotListener((value,error) ->{
//            if (error != null){
//                Log.e("Firestore", error.toString());
//            }
//            if (value != null && !value.isEmpty()){
//                eventList.clear();
//                for (QueryDocumentSnapshot snapshot: value){
//                    User owner = snapshot.getString("eventOwner");
//                    String eventName = snapshot.getString("eventName");
//                    String date = snapshot.getString("date");
//                    long totalSpots = snapshot.getLong("totalSpots") != null ? snapshot.getLong("totalSpots") : 0L;
//                    String location = snapshot.getString("location");
//                    long waitListLength = snapshot.getLong("waitListLength") != null ? snapshot.getLong("waitListLength") : 0L;
//                    String description = snapshot.getString("description");
//                    long entrantsLimit = snapshot.getLong("entrantsLimit") != null ? snapshot.getLong("entrantsLimit") : 0L;
//                    long sampleSize = snapshot.getLong("sampleSize") != null ? snapshot.getLong("sampleSize") : 0L;
//                    ArrayList<String> waitList = new ArrayList<>();// Figure out how to do waitlist
//
//                    eventList.add(new Event(eventOwner, eventName,date,location,totalSpots,waitListLength,description,entrantsLimit,sampleSize,waitList));
//                }
//            }
//        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
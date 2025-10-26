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
        events.addSnapshotListener((value,error) ->{
            if (error != null){
                Log.e("Firestore", error.toString());
            }
            if (value != null && !value.isEmpty()){
                eventList.clear();
                for (QueryDocumentSnapshot snapshot: value){
                    String eventName = snapshot.getString("eventName");
                    String date = snapshot.getString("date");
                    int totalSpots = Integer.parseInt(snapshot.getString("totalSpots"));
                    String location = snapshot.getString("location");
                    int waitListLength = Integer.parseInt(snapshot.getString("waitListLength"));
                    String description = snapshot.getString("description");
                    int entrantsLimit = Integer.parseInt(snapshot.getString("entrantsLimit"));
                    int sampleSize = Integer.parseInt(snapshot.getString("sampleSize"));
                    ArrayList<String> waitList = new ArrayList<>(); // Figure out how to do waitlist

                    eventList.add(new Event(eventName,date,location,totalSpots,waitListLength,description,entrantsLimit,sampleSize,waitList));
                }
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
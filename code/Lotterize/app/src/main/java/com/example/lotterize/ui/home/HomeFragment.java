package com.example.lotterize.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.lotterize.Event;
import com.example.lotterize.databinding.FragmentEventsRegisteredBinding;
import com.example.lotterize.databinding.FragmentHomeBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private FirebaseFirestore db;
    private CollectionReference events;
    private FragmentHomeBinding binding;
    private ArrayList<DocumentSnapshot> eventList;
    private ImageButton info;

    private TextView info_text;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        eventList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        events = db.collection("events");
        events.orderBy("date", Query.Direction.DESCENDING).limit(10).get().addOnSuccessListener(snapshot -> {
            eventList.addAll(snapshot.getDocuments());
        });

        info = binding.infoButton;

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InfoFragment i = new InfoFragment();
                i.show(getActivity().getSupportFragmentManager(), "Info");
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
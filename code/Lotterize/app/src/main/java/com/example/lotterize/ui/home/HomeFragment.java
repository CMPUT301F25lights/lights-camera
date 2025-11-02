package com.example.lotterize.ui.home;

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


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.lotterize.databinding.FragmentHomeBinding;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
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
        eventsArray = new EventListArrayAdapter(requireContext(), eventList);
        db = FirebaseFirestore.getInstance();
        events = db.collection("events");

        events.orderBy("registrationDeadline").whereGreaterThan("registrationDeadline", Timestamp.now())
                .get().addOnSuccessListener(snapshot -> {
            eventList.addAll(snapshot.getDocuments());
            eventsArray.notifyDataSetChanged();
        });

        info = binding.infoButton;
        eventsListView = binding.eventsList;
        TextInputEditText searchBar = binding.searchBar;

        eventsListView.setAdapter(eventsArray);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

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
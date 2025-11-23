package com.example.lotterize.ui.admin.adminEvents;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotterize.R;
import com.example.lotterize.ui.admin.adminEvents.AdminEventsAdapter;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminEventsFragment extends Fragment {

    private RecyclerView recyclerView;
    private AdminEventsAdapter adapter;
    private final List<Map<String, Object>> eventsList = new ArrayList<>();
    private final List<Map<String, Object>> shownList = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_admin_events, container, false);
        recyclerView = root.findViewById(R.id.recycler_admin_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        db = FirebaseFirestore.getInstance();
        adapter = new AdminEventsAdapter(getContext(), shownList);
        recyclerView.setAdapter(adapter);
        loadEvents();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextInputEditText searchBar = view.findViewById(R.id.admin_search_bar);
                searchBar.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {}

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        shownList.clear();
                        adapter.notifyDataSetChanged();
                        String search = s.toString().toLowerCase();
                        if (search.isEmpty()){
                            shownList.addAll(eventsList);
                        } else {
                            for (Map<String, Object> event : eventsList){
                                if (event.get("eventName") != null){
                                    String eventName = (String) event.get("eventName");
                                    if (eventName.toLowerCase().contains(search)) {
                                        shownList.add(event);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        }
                    }
                });
    }

    private void loadEvents() {
        db.collection("events")
                .addSnapshotListener((queryDocumentSnapshots , e) -> {
                    if (e != null) {
                        Toast.makeText(getContext(), "couldn't update data - AdminEventsFragment", Toast.LENGTH_SHORT).show();
                    } else {
                        eventsList.clear();
                        shownList.clear();
                        if (queryDocumentSnapshots != null) {
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                Map<String, Object> event = new HashMap<>(doc.getData());
                                event.put("id", doc.getId());
                                eventsList.add(event);
                                shownList.add(event);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }

                });
    }
}

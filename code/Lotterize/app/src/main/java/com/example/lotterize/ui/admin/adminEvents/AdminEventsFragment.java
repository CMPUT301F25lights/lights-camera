package com.example.lotterize.ui.admin.adminEvents;

import android.os.Bundle;
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
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_admin_events, container, false);
        recyclerView = root.findViewById(R.id.recycler_admin_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        db = FirebaseFirestore.getInstance();

        loadEvents();
        return root;
    }

    private void loadEvents() {
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventsList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Map<String, Object> event = new HashMap<>(doc.getData());
                        event.put("id", doc.getId());
                        eventsList.add(event);
                    }
                    adapter = new AdminEventsAdapter(getContext(), eventsList);
                    recyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show()
                );
    }
}

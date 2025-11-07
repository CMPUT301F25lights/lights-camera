package com.example.lotterize.ui.eventsRegistered;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.lotterize.R;
import com.example.lotterize.ui.eventsRegistered.EventsRegisteredArrayAdapter;
import com.example.lotterize.ui.eventsRegistered.EventsRegisteredViewModel;

import java.util.ArrayList;

public class EventsRegisteredFragment extends Fragment {

    private EventsRegisteredViewModel viewModel;
    private EventsRegisteredArrayAdapter adapter;
    private ListView listView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_events_registered, container, false);

        listView = root.findViewById(R.id.listEventsRegistered);
        adapter = new EventsRegisteredArrayAdapter(getContext(), new ArrayList<>());
        listView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(EventsRegisteredViewModel.class);

        viewModel.getRegisteredEvents().observe(getViewLifecycleOwner(), events -> {
            adapter.updateData(events);
        });

        return root;
    }
}

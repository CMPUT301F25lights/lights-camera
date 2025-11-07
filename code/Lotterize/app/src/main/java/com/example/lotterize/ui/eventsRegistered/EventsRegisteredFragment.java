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

/**
 * {@code EventsRegisteredFragment} displays a list of events that the current user
 * has registered for within the Lotterize application.
 * It uses a {@link ListView} with a {@link EventsRegisteredArrayAdapter} to present
 * the list of events, and observes data from the {@link EventsRegisteredViewModel}
 * to automatically update the UI when the data changes.
 *
 * This fragment follows the MVVM architecture, keeping the UI logic separate from
 * the data management handled by the ViewModel.
 */
public class EventsRegisteredFragment extends Fragment {

    private EventsRegisteredViewModel viewModel;
    private EventsRegisteredArrayAdapter adapter;
    private ListView listView;

    /**
     * Called to have the fragment instantiate its user interface view.
     * This method inflates the layout, initializes the ListView and its adapter,
     * and sets up LiveData observation from the {@link EventsRegisteredViewModel}
     * to automatically update the displayed list of registered events.
     *
     * @param inflater  The LayoutInflater object that can be used to inflate views in the fragment.
     * @param container The parent view that the fragment’s UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-created from a previous saved state.
     * @return The root {@link View} for the fragment's UI.
     */
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

package com.example.lotterize.ui.addEvents;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotterize.Event;
import com.example.lotterize.R;
import com.example.lotterize.databinding.FragmentAddEventsBinding;

import java.util.ArrayList;

/**
 * This is a fragment that displays the user's events and allows creating or editing events.
 * It sets up a {@link ListView} backed by {@link MyEventsArrayAdapter}, observes
 * {@link AddEventsViewModel} for updates, and navigates to edit or create screens.
 */
public class AddEventsFragment extends Fragment {

    /** View binding for the add events fragment layout. */
    private FragmentAddEventsBinding binding;
    private MyEventsArrayAdapter myEventsArrayAdapter;
    private AddEventsViewModel viewModel;

    /**
     * This inflates the fragment layout, initializes the ListView and adapter,
     * wires up item click navigation to the edit screen, observes the events LiveData,
     * and sets the click handler for creating a new event.
     *
     * @param inflater
     *      The LayoutInflater used to inflate views in this fragment
     * @param container
     *      The parent view that the fragment's UI will attach to (if non-null)
     * @param savedInstanceState
     *      If non-null, this fragment is being re-created from a previous state
     * @return
     *      Returns the root view of the fragment layout
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        viewModel = new ViewModelProvider(this).get(AddEventsViewModel.class);
        binding = FragmentAddEventsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // ListView + adapter
        ListView listViewMyEvents = binding.recyclerEvents;
        myEventsArrayAdapter = new MyEventsArrayAdapter(requireContext(), new ArrayList<>());
        listViewMyEvents.setAdapter(myEventsArrayAdapter);

        // Navigate to edit screen when an event is tapped
        listViewMyEvents.setOnItemClickListener((parent, view, position, id) -> {
            Event selectedEvent = (Event) parent.getItemAtPosition(position);

            Bundle args = new Bundle();
            args.putString("eventId", selectedEvent.getEventId());

            NavHostFragment.findNavController(AddEventsFragment.this)
                    .navigate(R.id.navigation_editEvents, args);
        });

        // Observe the events list and update the adapter when data changes
        viewModel.getMyEvent().observe(getViewLifecycleOwner(), events -> {
            myEventsArrayAdapter.updateData(new ArrayList<>(events));
        });

        binding.buttonNewEvent.setOnClickListener(v ->
                NavHostFragment.findNavController(AddEventsFragment.this)
                        .navigate(R.id.navigation_newEvent)
        );

        return root;
    }


    /**
     * This clears the binding reference when the view is destroyed to avoid memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
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

public class AddEventsFragment extends Fragment {

    private FragmentAddEventsBinding binding;
    private MyEventsArrayAdapter myEventsArrayAdapter;
    private AddEventsViewModel viewModel;

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


        listViewMyEvents.setOnItemClickListener((parent, view, position, id) -> {
            Event selectedEvent = (Event) parent.getItemAtPosition(position);

            Bundle args = new Bundle();
            args.putString("eventId", selectedEvent.getEventId());

            NavHostFragment.findNavController(AddEventsFragment.this)
                    .navigate(R.id.navigation_editEvents, args);
        });

        // Observe the events list
        viewModel.getMyEvent().observe(getViewLifecycleOwner(), events -> {
            myEventsArrayAdapter.updateData(new ArrayList<>(events));
        });

        binding.buttonNewEvent.setOnClickListener(v ->
                NavHostFragment.findNavController(AddEventsFragment.this)
                        .navigate(R.id.navigation_newEvent)
        );

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

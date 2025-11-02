package com.example.lotterize.ui.addEvents;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotterize.R;
import com.example.lotterize.databinding.FragmentAddEventsBinding;

public class AddEventsFragment extends Fragment {

    private FragmentAddEventsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AddEventsViewModel addEventsViewModel =
                new ViewModelProvider(this).get(AddEventsViewModel.class);

        binding = FragmentAddEventsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //final TextView textView = binding.textAddEvents;
        //addEventsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        binding.buttonNewEvent.setOnClickListener(v -> {
            NavHostFragment.findNavController(AddEventsFragment.this)
                    .navigate(R.id.navigation_newEvent);
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
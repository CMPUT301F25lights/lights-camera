package com.example.lotterize.ui.eventsRegistered;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.lotterize.databinding.FragmentEventsRegisteredBinding;

public class EventsRegisteredFragment extends Fragment {

    private FragmentEventsRegisteredBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        EventsRegisteredViewModel dashboardViewModel =
                new ViewModelProvider(this).get(EventsRegisteredViewModel.class);

        binding = FragmentEventsRegisteredBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textEventsregistered;
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
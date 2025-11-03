package com.example.lotterize.ui.addEvents;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotterize.R;
import com.example.lotterize.Event;
import com.example.lotterize.databinding.FragmentAllEntrantsBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
public class AllEntrantsFragment extends Fragment {

    private FragmentAllEntrantsBinding binding;
    private FirebaseFirestore db;
    private String eventId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAllEntrantsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        eventId = getEventIdArg();
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(requireContext(), "Missing eventId", Toast.LENGTH_SHORT).show();
        }

        binding.backButton.setOnClickListener(view ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        // Open reusable list screen with a status flag
        binding.rowWaitlistList.setOnClickListener(view -> openList("WAITLIST"));
        binding.rowChosenList.setOnClickListener(view -> openList("CHOSEN"));
        binding.rowCancelledList.setOnClickListener(view -> openList("CANCELLED"));
        binding.rowEnrolledList.setOnClickListener(view -> openList("ENROLLED"));

        binding.rowWaitlistMap.setOnClickListener(view ->
                Toast.makeText(requireContext(), "Map coming soon", Toast.LENGTH_SHORT).show()
        );

        // TODOs for the bottom buttons
        binding.btnExportCsv.setOnClickListener(v1 -> {/* export */});
        binding.btnNotifyCancelled.setOnClickListener(v12 -> {/* notify */});
        binding.btnNotifyChosen.setOnClickListener(v13 -> {/* notify */});
        binding.btnNotifyWaitlist.setOnClickListener(v14 -> {/* notify */});
        binding.btnDrawReplacement.setOnClickListener(v15 -> {/* draw */});
    }

    private void openList(@NonNull String status) {
        if (eventId == null) {
            Toast.makeText(requireContext(), "Missing eventId", Toast.LENGTH_SHORT).show();
            return;
        }
        Bundle b = new Bundle();
        b.putString("eventId", eventId);
        b.putString("status", status);
        NavHostFragment.findNavController(this).navigate(R.id.navigation_entrantList, b);
    }

    private @Nullable String getEventIdArg() {
        Bundle args = getArguments();
        if (args == null) return null;
        String v = args.getString("eventId");
        return (v == null || v.isEmpty()) ? null : v;
    }
}

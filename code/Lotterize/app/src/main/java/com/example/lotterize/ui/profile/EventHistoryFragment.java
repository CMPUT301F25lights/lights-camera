package com.example.lotterize.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.lotterize.CurrentUser;
import com.example.lotterize.R;
import com.example.lotterize.User;
import com.example.lotterize.databinding.FragmentEventHistoryBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

public class EventHistoryFragment extends Fragment {

    private static final String TAG = "EventHistoryFragment";
    private FragmentEventHistoryBinding binding;
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventHistoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        db = FirebaseFirestore.getInstance();

        // Hide bottom navigation
        if (getActivity() != null) {
            BottomNavigationView navView = getActivity().findViewById(R.id.nav_view);
            if (navView != null) {
                navView.setVisibility(View.GONE);
            }
        }

        // Get current user ID (the string document ID)
        User currentUser = CurrentUser.get();
        if (currentUser != null && currentUser.getUserId() != null) {
            currentUserId = currentUser.getUserId();
            Log.d(TAG, "Current user ID: " + currentUserId);
        } else {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "User or userId is null");
            return root;
        }

        // Back button
        binding.buttonBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigateUp();
        });

        // Load event history
        loadEventHistory();

        return root;
    }

    private void loadEventHistory() {
        if (currentUserId == null || currentUserId.isEmpty()) {
            Log.e(TAG, "Cannot load event history - userId is null or empty");
            return;
        }

        db.collection("events")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    binding.eventHistoryContainer.removeAllViews();

                    int eventCount = 0;

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        List<String> waitList = (List<String>) doc.get("waitList");
                        List<String> selectedList = (List<String>) doc.get("selectedList");
                        List<String> finalList = (List<String>) doc.get("finalList");
                        List<String> cancelledList = (List<String>) doc.get("cancelledList");

                        String eventName = doc.getString("eventName");

                        Log.d(TAG, "Checking event: " + eventName);
                        Log.d(TAG, "waitList: " + waitList);
                        Log.d(TAG, "selectedList: " + selectedList);
                        Log.d(TAG, "finalList: " + finalList);

                        String status = null;

                        // Priority: selectedList or finalList = "Was Selected"
                        if ((selectedList != null && selectedList.contains(currentUserId)) ||
                                (finalList != null && finalList.contains(currentUserId))) {
                            status = "Was Selected";
                            eventCount++;
                            addEventToView(eventName, status);
                            Log.d(TAG, "User WAS SELECTED for: " + eventName);
                        }
                        // Check if user is in waitList (and NOT selected)
                        else if (waitList != null && waitList.contains(currentUserId)) {
                            status = "Was Not Selected";
                            eventCount++;
                            addEventToView(eventName, status);
                            Log.d(TAG, "User WAS NOT SELECTED (on waitlist) for: " + eventName);
                        }
                        // Check if user cancelled
                        else if (cancelledList != null && cancelledList.contains(currentUserId)) {
                            status = "Cancelled";
                            eventCount++;
                            addEventToView(eventName, status);
                            Log.d(TAG, "User CANCELLED: " + eventName);
                        }
                    }

                    // Show empty state if no events found
                    if (eventCount == 0) {
                        binding.textEmptyState.setVisibility(View.VISIBLE);
                        Log.d(TAG, "No events found for user");
                    }

                    Log.d(TAG, "Total events loaded: " + eventCount);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading events: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "Error loading events", Toast.LENGTH_SHORT).show();
                });
    }

    private void addEventToView(String eventName, String status) {
        // Create horizontal layout for event item
        LinearLayout eventItem = new LinearLayout(getContext());
        eventItem.setOrientation(LinearLayout.HORIZONTAL);
        eventItem.setPadding(0, 16, 0, 16);

        // Event name (left)
        TextView eventNameView = new TextView(getContext());
        eventNameView.setText(eventName != null ? eventName : "Unknown Event");
        eventNameView.setTextColor(0xFF000000);
        eventNameView.setTextSize(18);
        eventNameView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        eventNameView.setLayoutParams(nameParams);

        // Status (right)
        TextView statusView = new TextView(getContext());
        statusView.setText(status);
        statusView.setTextColor(0xFF666666);
        statusView.setTextSize(16);
        statusView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);

        eventItem.addView(eventNameView);
        eventItem.addView(statusView);

        // Add divider
        View divider = new View(getContext());
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                1
        );
        dividerParams.setMargins(0, 0, 0, 0);
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(0xFFE0E0E0);

        binding.eventHistoryContainer.addView(eventItem);
        binding.eventHistoryContainer.addView(divider);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Show bottom navigation when leaving
        if (getActivity() != null) {
            BottomNavigationView navView = getActivity().findViewById(R.id.nav_view);
            if (navView != null) {
                navView.setVisibility(View.VISIBLE);
            }
        }

        binding = null;
    }
}
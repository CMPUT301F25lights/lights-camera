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

/**
 * The EventHistoryFragment class displays a list of events that the current user
 * has participated in, joined the waitlist for, or cancelled.
 * It retrieves event data from Firebase Firestore and dynamically populates
 * the screen with event names and participation statuses.
 */
public class EventHistoryFragment extends Fragment {

    // Log tag for debugging purposes
    private static final String TAG = "EventHistoryFragment";

    private FragmentEventHistoryBinding binding;

    private FirebaseFirestore db;

    // Current user’s unique document ID
    private String currentUserId;

    /**
     * Called when the fragment should create its view hierarchy.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentEventHistoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Hide bottom navigation when viewing event history
        if (getActivity() != null) {
            BottomNavigationView navView = getActivity().findViewById(R.id.nav_view);
            if (navView != null) {
                navView.setVisibility(View.GONE);
            }
        }

        // Retrieve the current user’s ID
        User currentUser = CurrentUser.get();
        if (currentUser != null && currentUser.getUserId() != null) {
            currentUserId = currentUser.getUserId();
            Log.d(TAG, "Current user ID: " + currentUserId);
        } else {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "User or userId is null");
            return root;
        }

        // Back button navigates to the previous screen
        binding.buttonBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigateUp();
        });

        // Load all events for the current user
        loadEventHistory();

        return root;
    }

    /**
     * Retrieves all events from Firestore and filters them based on the user’s participation.
     * The method determines whether the user was selected, waitlisted, or cancelled.
     */
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

                    // Loop through all event documents
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        List<String> waitList = (List<String>) doc.get("waitList");
                        List<String> selectedList = (List<String>) doc.get("selectedList");
                        List<String> finalList = (List<String>) doc.get("finalList");
                        List<String> cancelledList = (List<String>) doc.get("cancelledList");

                        String eventName = doc.getString("eventName");
                        Log.d(TAG, "Checking event: " + eventName);

                        String status = null;

                        // Priority: if selected or finalized, mark as "Was Selected"
                        if ((selectedList != null && selectedList.contains(currentUserId)) ||
                                (finalList != null && finalList.contains(currentUserId))) {
                            status = "Was Selected";
                            eventCount++;
                            addEventToView(eventName, status);
                            Log.d(TAG, "User WAS SELECTED for: " + eventName);
                        }
                        // If in waitlist but not selected
                        else if (waitList != null && waitList.contains(currentUserId)) {
                            status = "Was Not Selected";
                            eventCount++;
                            addEventToView(eventName, status);
                            Log.d(TAG, "User WAS NOT SELECTED (on waitlist) for: " + eventName);
                        }
                        // If user cancelled
                        else if (cancelledList != null && cancelledList.contains(currentUserId)) {
                            status = "Cancelled";
                            eventCount++;
                            addEventToView(eventName, status);
                            Log.d(TAG, "User CANCELLED: " + eventName);
                        }
                    }

                    // Show placeholder if no events match
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

    /**
     * Dynamically adds a single event item (name + status) to the scrollable event history view.
     *
     * @param eventName The name of the event.
     * @param status    The user’s participation status in the event.
     */
    private void addEventToView(String eventName, String status) {
        // Create a horizontal layout for each event row
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
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        );
        eventNameView.setLayoutParams(nameParams);

        // Event status (right)
        TextView statusView = new TextView(getContext());
        statusView.setText(status);
        statusView.setTextColor(0xFF666666);
        statusView.setTextSize(16);
        statusView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);

        eventItem.addView(eventNameView);
        eventItem.addView(statusView);

        // Divider line between items
        View divider = new View(getContext());
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1
        );
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(0xFFE0E0E0);

        binding.eventHistoryContainer.addView(eventItem);
        binding.eventHistoryContainer.addView(divider);
    }

    /**
     * Called when the fragment’s view is destroyed.
     * Ensures the bottom navigation bar reappears when returning to other screens.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Re-show bottom navigation
        if (getActivity() != null) {
            BottomNavigationView navView = getActivity().findViewById(R.id.nav_view);
            if (navView != null) {
                navView.setVisibility(View.VISIBLE);
            }
        }

        binding = null;
    }
}

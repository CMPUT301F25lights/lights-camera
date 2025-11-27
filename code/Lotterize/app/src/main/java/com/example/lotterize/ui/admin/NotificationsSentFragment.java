package com.example.lotterize.ui.admin;

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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * Fragment that displays all notifications sent by the current user.
 * Retrieves notifications from Firestore and displays them in a scrollable list.
 */
public class NotificationsSentFragment extends Fragment {

    private static final String TAG = "NotificationsSent";
    private FirebaseFirestore db;
    private String currentUserId;
    private LinearLayout notificationsContainer;
    private TextView textEmptyState;

    /**
     * Called when the fragment should create its view hierarchy.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_notifications_sent, container, false);

        // Initialize views
        notificationsContainer = root.findViewById(R.id.notifications_container);
        textEmptyState = root.findViewById(R.id.text_empty_state);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Hide bottom navigation
        if (getActivity() != null) {
            BottomNavigationView navView = getActivity().findViewById(R.id.nav_view_admin);
            if (navView != null) {
                navView.setVisibility(View.GONE);
            }
        }

        // Get current user ID
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
        root.findViewById(R.id.button_back).setOnClickListener(v -> {
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_admin).popBackStack();
        });


        // Load sent notifications
        loadSentNotifications();

        return root;
    }

    /**
     * Retrieves all notifications sent by the current user from Firestore.
     */
    private void loadSentNotifications() {
        if (currentUserId == null || currentUserId.isEmpty()) {
            Log.e(TAG, "Cannot load notifications - userId is null or empty");
            return;
        }

        Log.d(TAG, "Loading sent notifications for user: " + currentUserId);

        db.collection("notifications")
                .whereEqualTo("senderId", currentUserId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    notificationsContainer.removeAllViews();
                    int notificationCount = 0;

                    // Loop through all notification documents
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String message = doc.getString("message");
                        String eventName = doc.getString("eventName");

                        if (message != null && !message.isEmpty()) {
                            addNotificationToView(message);
                            notificationCount++;
                            Log.d(TAG, "Loaded notification: " + message);
                        }
                    }

                    // Show empty state if no notifications found
                    if (notificationCount == 0) {
                        textEmptyState.setVisibility(View.VISIBLE);
                        Log.d(TAG, "No notifications found");
                    } else {
                        textEmptyState.setVisibility(View.GONE);
                    }

                    Log.d(TAG, "Total notifications loaded: " + notificationCount);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading notifications: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "Error loading notifications", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Dynamically adds a single notification to the scrollable view.
     *
     * @param message The notification message
     */
    private void addNotificationToView(String message) {
        // Create a TextView for each notification
        TextView notificationView = new TextView(getContext());
        notificationView.setText(message);
        notificationView.setTextColor(0xFF000000);
        notificationView.setTextSize(16);
        notificationView.setPadding(0, 16, 0, 16);

        notificationsContainer.addView(notificationView);
    }

    /**
     * Called when the fragment's view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Show bottom navigation when leaving
        if (getActivity() != null) {
            BottomNavigationView navView = getActivity().findViewById(R.id.nav_view_admin);
            if (navView != null) {
                navView.setVisibility(View.VISIBLE);
            }
        }
    }
}
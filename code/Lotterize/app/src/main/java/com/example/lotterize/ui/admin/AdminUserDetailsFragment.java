package com.example.lotterize.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.lotterize.R;
import com.example.lotterize.databinding.FragmentAdminUserDetailsBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

/**
 * Fragment that displays detailed user information for admin review.
 * Allows admin to view user details and perform cascade delete of the account.
 */
public class AdminUserDetailsFragment extends Fragment {

    private static final String TAG = "AdminUserDetails";
    private FragmentAdminUserDetailsBinding binding;
    private FirebaseFirestore db;
    private String userId;
    private String username;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAdminUserDetailsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();

        // Get userId from arguments
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }

        if (userId == null) {
            Toast.makeText(getContext(), "Error: No user selected", Toast.LENGTH_SHORT).show();
        } else {
            loadUserData(userId);
        }

        // Received Notifications button
        root.findViewById(R.id.btn_received_notifications).setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.navigation_notifications_received);
        });

        // Sent Notifications button
        root.findViewById(R.id.btn_sent_notifications).setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.navigation_notifications_sent);
        });

        // Back button click
        binding.buttonBack.setOnClickListener(v -> {
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_admin).popBackStack();
        });

        // Remove Account button with cascade delete
        binding.buttonRemove.setOnClickListener(v -> {
            showDeleteConfirmation();
        });

        // Hide bottom navigation bar
        if (getActivity() != null) {
            BottomNavigationView navView = getActivity().findViewById(R.id.nav_view_admin);
            if (navView != null) {
                navView.setVisibility(View.GONE);
            }
        }

        return root;
    }

    /**
     * Loads user data from Firestore and displays it.
     *
     * @param userId The user's document ID
     */
    private void loadUserData(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(this::setUserDetails)
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show());
    }

    /**
     * Populates the UI with user details from Firestore.
     *
     * @param document The Firestore document containing user data
     */
    private void setUserDetails(DocumentSnapshot document) {
        if (document.exists()) {
            username = document.getString("username");
            String name = document.getString("name");
            String email = document.getString("email");
            String phone = document.getString("phoneNumber");

            binding.textNameValue.setText(name != null && !name.isEmpty() ? name : "Not set");
            binding.textEmailValue.setText(email != null && !email.isEmpty() ? email : "Not set");
            binding.textPhoneValue.setText(phone != null && !phone.isEmpty() ? phone : "Not set");
        } else {
            Toast.makeText(getContext(), "User does not exist", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Shows a confirmation dialog before deleting the user account.
     */
    private void showDeleteConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Remove User Account")
                .setMessage("Are you sure you want to remove user '" + username + "'?\n\n" +
                        "This will:\n" +
                        "• Remove them from all events\n" +
                        "• Delete all events they created\n" +
                        "• Permanently delete their account\n\n" +
                        "This action cannot be undone.")
                .setPositiveButton("Remove", (dialog, which) -> performCascadeDelete())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Performs a cascade delete that removes the user from all events and deletes
     * all events they created before deleting their account.
     */
    private void performCascadeDelete() {
        if (userId == null) {
            Toast.makeText(requireContext(), "Error: User ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Starting cascade delete for user: " + username);

        // Step 1: Remove user from all events (waitList, selectedList, etc.)
        db.collection("events")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot eventDoc : querySnapshot) {
                        // Get all user lists from the event
                        List<String> waitList = (List<String>) eventDoc.get("waitList");
                        List<String> selectedList = (List<String>) eventDoc.get("selectedList");
                        List<String> finalList = (List<String>) eventDoc.get("finalList");
                        List<String> cancelledList = (List<String>) eventDoc.get("cancelledList");

                        boolean needsUpdate = false;

                        // Remove user from all lists
                        if (waitList != null && waitList.remove(userId)) {
                            needsUpdate = true;
                        }
                        if (selectedList != null && selectedList.remove(userId)) {
                            needsUpdate = true;
                        }
                        if (finalList != null && finalList.remove(userId)) {
                            needsUpdate = true;
                        }
                        if (cancelledList != null && cancelledList.remove(userId)) {
                            needsUpdate = true;
                        }

                        // Update the event if user was found in any list
                        if (needsUpdate) {
                            eventDoc.getReference()
                                    .update(
                                            "waitList", waitList,
                                            "selectedList", selectedList,
                                            "finalList", finalList,
                                            "cancelledList", cancelledList
                                    )
                                    .addOnSuccessListener(aVoid ->
                                            Log.d(TAG, "Removed user from event: " + eventDoc.getString("eventName")))
                                    .addOnFailureListener(e ->
                                            Log.e(TAG, "Error removing user from event", e));
                        }
                    }

                    // Step 2: Delete events owned by this user
                    deleteEventsOwnedByUser();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error removing user from events: " + e.getMessage(), e);
                    Toast.makeText(requireContext(), "Error during account removal", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Deletes all events created by the specified user.
     */
    private void deleteEventsOwnedByUser() {
        db.collection("events")
                .whereEqualTo("ownerId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int eventCount = querySnapshot.size();
                    Log.d(TAG, "Found " + eventCount + " events owned by user: " + username);

                    // Delete each event owned by the user
                    for (QueryDocumentSnapshot eventDoc : querySnapshot) {
                        eventDoc.getReference()
                                .delete()
                                .addOnSuccessListener(aVoid ->
                                        Log.d(TAG, "Deleted event: " + eventDoc.getString("eventName")))
                                .addOnFailureListener(e ->
                                        Log.e(TAG, "Error deleting event", e));
                    }

                    // Step 3: Finally delete the user document
                    finalizeAccountDeletion(eventCount);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error finding user's events: " + e.getMessage(), e);
                    // Still try to delete the user even if this fails
                    finalizeAccountDeletion(0);
                });
    }

    /**
     * Finalizes the account deletion by removing the user document from Firestore.
     *
     * @param eventCount Number of events that were deleted
     */
    private void finalizeAccountDeletion(int eventCount) {
        db.collection("users")
                .document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User deleted: " + username);

                    String message = "User " + username + " removed successfully.";
                    if (eventCount > 0) {
                        message += " " + eventCount + " event(s) also deleted.";
                    }
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();

                    // Navigate back to admin profiles list
                    Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_admin).popBackStack();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting user: " + e.getMessage(), e);
                    Toast.makeText(requireContext(), "Error removing user account", Toast.LENGTH_SHORT).show();
                });
    }

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

        binding = null;
    }
}
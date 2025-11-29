package com.example.lotterize.ui.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.lotterize.MainActivity;
import com.example.lotterize.CurrentUser;
import com.example.lotterize.R;
import com.example.lotterize.databinding.FragmentProfileBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

/**
 * The ProfileFragment class manages the user profile screen in the Lotterize app.
 * It displays user information, such as their username, and provides options
 * to navigate to account details, event history, delete the account, or log out.
 */
public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private FragmentProfileBinding binding;
    private ProfileViewModel profileViewModel;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Ensure bottom navigation is visible when viewing the profile
        showBottomNavigation();

        // Observe user data changes and update greeting text based on username
        profileViewModel.getUserData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                String username = user.getUsername() != null ? user.getUsername() : "User";
                String greeting = "Hello, " + username + "!";
                binding.textGreeting.setText(greeting);
            }
        });

        // Delete Account with cascade deletion
        binding.buttonDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to permanently delete your account?\n\n" +
                            "This will:\n" +
                            "• Remove you from all events\n" +
                            "• Delete all events you created\n" +
                            "• Delete all notifications you sent/received\n" +
                            "• Permanently delete your account\n\n" +
                            "This action cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> performCascadeDelete())
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Logout
        binding.buttonLogout.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        // Navigate to Account Fragment when account section is clicked
        View.OnClickListener accountClickListener = v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.navigation_account);
        };

        binding.iconAccount.setOnClickListener(accountClickListener);
        binding.textAccount.setOnClickListener(accountClickListener);
        binding.textAccountDesc.setOnClickListener(accountClickListener);

        // Navigate to Event History Fragment when event section is clicked
        View.OnClickListener eventHistoryClickListener = v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.navigation_event_history);
        };

        binding.iconEvent.setOnClickListener(eventHistoryClickListener);
        binding.textEvent.setOnClickListener(eventHistoryClickListener);
        binding.textEventDesc.setOnClickListener(eventHistoryClickListener);

        // Notification toggle
        binding.switchNotifications.setChecked(CurrentUser.get().getWantNotification());

        binding.switchNotifications.setOnClickListener(v -> {
            boolean enabled = binding.switchNotifications.isChecked();

            // Update in-memory current user
            CurrentUser.get().setWantNotification(enabled);

            // Update Firestore
            String userId = CurrentUser.get().getUserId();

            db.collection("users")
                    .document(userId)
                    .update("wantNotification", enabled)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(requireContext(), enabled ? "Notifications turned on" : "Opted out of notifications", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to update notification preference", e);
                    });
        });

        return root;
    }

    /**
     * Performs a cascade delete that removes:
     * - User from all events (waitList, selectedList, finalList, cancelledList)
     * - All events created by the user
     * - All notifications sent by the user
     * - All notifications received by the user
     * - The user's account document
     */
    private void performCascadeDelete() {
        String userId = CurrentUser.get().getUserId();
        String username = CurrentUser.get().getUsername();

        if (userId == null || userId.isEmpty()) {
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
                    deleteEventsOwnedByUser(userId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error removing user from events: " + e.getMessage(), e);
                    Toast.makeText(requireContext(), "Error during account deletion", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Deletes all events created by the current user.
     */
    private void deleteEventsOwnedByUser(String userId) {
        db.collection("events")
                .whereEqualTo("ownerId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int eventCount = querySnapshot.size();
                    Log.d(TAG, "Found " + eventCount + " events owned by user");

                    // Delete each event owned by the user
                    for (QueryDocumentSnapshot eventDoc : querySnapshot) {
                        eventDoc.getReference()
                                .delete()
                                .addOnSuccessListener(aVoid ->
                                        Log.d(TAG, "Deleted event: " + eventDoc.getString("eventName")))
                                .addOnFailureListener(e ->
                                        Log.e(TAG, "Error deleting event", e));
                    }

                    // Step 3: Delete notifications sent by this user
                    deleteNotificationsSentByUser(userId, eventCount);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error finding user's events: " + e.getMessage(), e);
                    // Continue with notification deletion even if this fails
                    deleteNotificationsSentByUser(userId, 0);
                });
    }

    /**
     * Deletes all notifications sent by the user.
     */
    private void deleteNotificationsSentByUser(String userId, int eventCount) {
        db.collection("notifications")
                .whereEqualTo("senderId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int sentCount = querySnapshot.size();
                    Log.d(TAG, "Found " + sentCount + " notifications sent by user");

                    // Delete each notification sent by the user
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        doc.getReference()
                                .delete()
                                .addOnSuccessListener(aVoid ->
                                        Log.d(TAG, "Deleted sent notification: " + doc.getId()))
                                .addOnFailureListener(e ->
                                        Log.e(TAG, "Error deleting sent notification", e));
                    }

                    // Step 4: Delete notifications received by this user
                    deleteNotificationsReceivedByUser(userId, eventCount, sentCount);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error finding sent notifications: " + e.getMessage(), e);
                    // Continue with received notifications even if this fails
                    deleteNotificationsReceivedByUser(userId, eventCount, 0);
                });
    }

    /**
     * Deletes all notifications received by the user
     * (where receiversId array contains the user's ID).
     */
    private void deleteNotificationsReceivedByUser(String userId, int eventCount, int sentCount) {
        db.collection("notifications")
                .whereArrayContains("receiversId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int receivedCount = querySnapshot.size();
                    Log.d(TAG, "Found " + receivedCount + " notifications received by user");

                    // Delete each notification where user is a receiver
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        doc.getReference()
                                .delete()
                                .addOnSuccessListener(aVoid ->
                                        Log.d(TAG, "Deleted received notification: " + doc.getId()))
                                .addOnFailureListener(e ->
                                        Log.e(TAG, "Error deleting received notification", e));
                    }

                    // Step 5: Finally delete the user document
                    finalizeAccountDeletion(eventCount, sentCount, receivedCount);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error finding received notifications: " + e.getMessage(), e);
                    // Still try to delete the user even if this fails
                    finalizeAccountDeletion(eventCount, sentCount, 0);
                });
    }

    /**
     * Finalizes the account deletion by removing the user document from Firestore
     * and logging the user out.
     *
     * @param eventCount Number of events that were deleted
     * @param sentCount Number of sent notifications deleted
     * @param receivedCount Number of received notifications deleted
     */
    private void finalizeAccountDeletion(int eventCount, int sentCount, int receivedCount) {
        String userId = CurrentUser.get().getUserId();
        String username = CurrentUser.get().getUsername();

        db.collection("users")
                .document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User deleted: " + username);

                    // Build detailed success message
                    StringBuilder message = new StringBuilder("Account deleted successfully.");
                    if (eventCount > 0) {
                        message.append("\n• ").append(eventCount).append(" event(s) deleted");
                    }
                    if (sentCount > 0) {
                        message.append("\n• ").append(sentCount).append(" sent notification(s) deleted");
                    }
                    if (receivedCount > 0) {
                        message.append("\n• ").append(receivedCount).append(" received notification(s) deleted");
                    }

                    Toast.makeText(requireContext(), message.toString(), Toast.LENGTH_LONG).show();

                    // Clear the current user session and navigate to login screen
                    CurrentUser.clear();
                    Intent intent = new Intent(requireContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting user: " + e.getMessage(), e);
                    Toast.makeText(requireContext(), "Error deleting account", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        showBottomNavigation();
    }

    /**
     * Makes the bottom navigation bar visible if it exists in the current activity.
     */
    private void showBottomNavigation() {
        if (getActivity() != null) {
            BottomNavigationView navView = getActivity().findViewById(R.id.nav_view);
            if (navView != null) {
                navView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
package com.example.lotterize.ui.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
 * Fragment responsible for displaying and managing the user's profile settings.
 *
 * <p>This fragment provides:
 * <ul>
 *   <li>Greeting and user information display</li>
 *   <li>Device linking toggle for automatic login</li>
 *   <li>Notification preference toggle</li>
 *   <li>Navigation to Account and Event History screens</li>
 *   <li>Account deletion with full cascade delete (events, notifications, lists)</li>
 *   <li>Logout functionality</li>
 * </ul>
 *
 * It connects to Firestore for all user-related operations and stores device link
 * preferences using {@link SharedPreferences}.
 */
public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private FragmentProfileBinding binding;
    private ProfileViewModel profileViewModel;
    private FirebaseFirestore db;

    /**
     * Inflates the view, sets up observers, listeners, and initializes UI components.
     *
     * @param inflater The layout inflater
     * @param container Parent view group
     * @param savedInstanceState Previously saved state, or null
     * @return Inflated and configured fragment view
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();

        showBottomNavigation();

        // Observe and update greeting message
        profileViewModel.getUserData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                String username = user.getUsername() != null ? user.getUsername() : "User";
                binding.textGreeting.setText("Hello, " + username + "!");
            }
        });

        // Account deletion dialog + cascade delete
        binding.buttonDelete.setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Account")
                        .setMessage("Are you sure you want to permanently delete your account?\n\n"
                                + "This will:\n"
                                + "• Remove you from all events\n"
                                + "• Delete all events you created\n"
                                + "• Delete all notifications you sent/received\n"
                                + "• Permanently delete your account\n\n"
                                + "This action cannot be undone.")
                        .setPositiveButton("Delete", (dialog, which) -> performCascadeDelete())
                        .setNegativeButton("Cancel", null)
                        .show()
        );

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

        // Device linking toggle
        binding.switchLinkDevice.setChecked(CurrentUser.get().getDeviceLinked());

        binding.switchLinkDevice.setOnClickListener(v -> {
            boolean enabled = binding.switchLinkDevice.isChecked();

            if (enabled) {
                // Show confirmation dialog
                new AlertDialog.Builder(requireContext())
                        .setTitle("Link Device")
                        .setMessage("This will allow automatic login on this device. " +
                                "Only enable this on devices you trust.\n\n" +
                                "Are you sure you want to link this device?")
                        .setPositiveButton("Yes, Link Device", (dialog, which) -> {
                            enableDeviceLinking();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            // Revert toggle
                            binding.switchLinkDevice.setChecked(false);
                        })
                        .show();
            } else {
                // Disable device linking
                disableDeviceLinking();
            }
        });

        return root;
    }

    /**
     * Enables device linking by saving user ID in SharedPreferences and updating Firestore.
     */
    private void enableDeviceLinking() {
        String userId = CurrentUser.get().getUserId();

        // Update in-memory current user
        CurrentUser.get().setDeviceLinked(true);

        // Save to SharedPreferences for automatic login
        android.content.SharedPreferences prefs = requireContext()
                .getSharedPreferences("LotterizePrefs", android.content.Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean("deviceLinked", true)
                .putString("linkedUserId", userId)
                .apply();

        // Update Firestore
        db.collection("users")
                .document(userId)
                .update("deviceLinked", true)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), "Device linked successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to link device", e);
                    Toast.makeText(requireContext(), "Error linking device", Toast.LENGTH_SHORT).show();
                    // Revert on failure
                    binding.switchLinkDevice.setChecked(false);
                    CurrentUser.get().setDeviceLinked(false);
                });
    }

    /**
     * Disables device linking by removing stored credentials
     */
    private void disableDeviceLinking() {
        String userId = CurrentUser.get().getUserId();

        // Update in-memory current user
        CurrentUser.get().setDeviceLinked(false);

        // Remove from SharedPreferences
        android.content.SharedPreferences prefs = requireContext()
                .getSharedPreferences("LotterizePrefs", android.content.Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean("deviceLinked", false)
                .remove("linkedUserId")
                .apply();

        // Update Firestore
        db.collection("users")
                .document(userId)
                .update("deviceLinked", false)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), "Device unlinked successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to unlink device", e);
                    Toast.makeText(requireContext(), "Error unlinking device", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Starts the full cascade account deletion process.
     *
     * <p>This removes the user from:
     * <ul>
     *   <li>All event lists</li>
     *   <li>All owned events</li>
     *   <li>All sent/received notifications</li>
     * </ul>
     */
    private void performCascadeDelete() {
        String userId = CurrentUser.get().getUserId();
        String username = CurrentUser.get().getUsername();

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(requireContext(), "Error: User ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Starting cascade delete for user: " + username);

        db.collection("events")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot eventDoc : querySnapshot) {
                        List<String> waitList = (List<String>) eventDoc.get("waitList");
                        List<String> selectedList = (List<String>) eventDoc.get("selectedList");
                        List<String> finalList = (List<String>) eventDoc.get("finalList");
                        List<String> cancelledList = (List<String>) eventDoc.get("cancelledList");

                        boolean needsUpdate = false;

                        if (waitList != null && waitList.remove(userId)) needsUpdate = true;
                        if (selectedList != null && selectedList.remove(userId)) needsUpdate = true;
                        if (finalList != null && finalList.remove(userId)) needsUpdate = true;
                        if (cancelledList != null && cancelledList.remove(userId)) needsUpdate = true;

                        if (needsUpdate) {
                            eventDoc.getReference()
                                    .update("waitList", waitList,
                                            "selectedList", selectedList,
                                            "finalList", finalList,
                                            "cancelledList", cancelledList)
                                    .addOnSuccessListener(aVoid ->
                                            Log.d(TAG, "Removed user from event: " + eventDoc.getString("eventName")))
                                    .addOnFailureListener(e ->
                                            Log.e(TAG, "Error removing user from event", e));
                        }
                    }

                    deleteEventsOwnedByUser(userId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error removing user from events", e);
                    Toast.makeText(requireContext(), "Error during account deletion", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Deletes all events created by the user.
     *
     * @param userId ID of the user whose events are being deleted
     */
    private void deleteEventsOwnedByUser(String userId) {
        db.collection("events")
                .whereEqualTo("ownerId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int eventCount = querySnapshot.size();
                    Log.d(TAG, "Found " + eventCount + " events owned by user");

                    for (QueryDocumentSnapshot eventDoc : querySnapshot) {
                        eventDoc.getReference()
                                .delete()
                                .addOnSuccessListener(aVoid ->
                                        Log.d(TAG, "Deleted event: " + eventDoc.getString("eventName")))
                                .addOnFailureListener(e ->
                                        Log.e(TAG, "Error deleting event", e));
                    }

                    deleteNotificationsSentByUser(userId, eventCount);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error finding user's events", e);
                    deleteNotificationsSentByUser(userId, 0);
                });
    }

    /**
     * Deletes all notifications sent by the user.
     *
     * @param userId ID of the user
     * @param eventCount number of events deleted earlier in the cascade
     */
    private void deleteNotificationsSentByUser(String userId, int eventCount) {
        db.collection("notifications")
                .whereEqualTo("senderId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int sentCount = querySnapshot.size();
                    Log.d(TAG, "Found " + sentCount + " sent notifications");

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        doc.getReference()
                                .delete()
                                .addOnSuccessListener(aVoid ->
                                        Log.d(TAG, "Deleted sent notification: " + doc.getId()))
                                .addOnFailureListener(e ->
                                        Log.e(TAG, "Error deleting notification", e));
                    }

                    deleteNotificationsReceivedByUser(userId, eventCount, sentCount);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error finding sent notifications", e);
                    deleteNotificationsReceivedByUser(userId, eventCount, 0);
                });
    }

    /**
     * Deletes or updates all notifications where the user is a receiver.
     *
     * @param userId ID of the user
     * @param eventCount number of events deleted
     * @param sentCount number of notifications sent by the user
     */
    private void deleteNotificationsReceivedByUser(String userId, int eventCount, int sentCount) {
        db.collection("notifications")
                .whereArrayContains("receiversId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int receivedCount = querySnapshot.size();
                    Log.d(TAG, "Found " + receivedCount + " received notifications");

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        List<String> receivers = (List<String>) doc.get("receiversId");
                        if (receivers != null) {
                            receivers.remove(userId);

                            if (receivers.isEmpty()) {
                                doc.getReference()
                                        .delete()
                                        .addOnSuccessListener(aVoid ->
                                                Log.d(TAG, "Deleted notification with no receivers: " + doc.getId()))
                                        .addOnFailureListener(e ->
                                                Log.e(TAG, "Error deleting notification", e));
                            } else {
                                doc.getReference()
                                        .update("receiversId", receivers)
                                        .addOnSuccessListener(aVoid ->
                                                Log.d(TAG, "Updated notification: " + doc.getId()))
                                        .addOnFailureListener(e ->
                                                Log.e(TAG, "Error updating notification", e));
                            }
                        }
                    }

                    finalizeAccountDeletion(eventCount, sentCount, receivedCount);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error finding received notifications", e);
                    finalizeAccountDeletion(eventCount, sentCount, 0);
                });
    }

    /**
     * Final step in cascade delete: deletes the user document and resets app state.
     *
     * @param eventCount number of deleted events
     * @param sentCount number of deleted sent notifications
     * @param receivedCount number of deleted received notifications
     */
    private void finalizeAccountDeletion(int eventCount, int sentCount, int receivedCount) {
        String userId = CurrentUser.get().getUserId();
        String username = CurrentUser.get().getUsername();

        db.collection("users")
                .document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User deleted: " + username);

                    // Clear device linking
                    android.content.SharedPreferences prefs = requireContext()
                            .getSharedPreferences("LotterizePrefs", android.content.Context.MODE_PRIVATE);
                    prefs.edit()
                            .putBoolean("deviceLinked", false)
                            .remove("linkedUserId")
                            .apply();
                    StringBuilder message = new StringBuilder("Account deleted successfully.");
                    if (eventCount > 0) message.append("\n• ").append(eventCount).append(" event(s) deleted");
                    if (sentCount > 0) message.append("\n• ").append(sentCount).append(" sent notification(s)");
                    if (receivedCount > 0) message.append("\n• ").append(receivedCount).append(" received notification(s)");

                    Toast.makeText(requireContext(), message.toString(), Toast.LENGTH_LONG).show();

                    CurrentUser.clear();
                    Intent intent = new Intent(requireContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting user", e);
                    Toast.makeText(requireContext(), "Error deleting account", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Ensures bottom navigation bar is visible when this fragment is active.
     */
    @Override
    public void onResume() {
        super.onResume();
        showBottomNavigation();
    }

    /**
     * Makes the bottom navigation visible.
     */
    private void showBottomNavigation() {
        if (getActivity() != null) {
            BottomNavigationView navView = getActivity().findViewById(R.id.nav_view);
            if (navView != null) navView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Clears binding reference to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

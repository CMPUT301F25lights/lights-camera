package com.example.lotterize.ui.admin;

import android.content.Intent;
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
 * Fragment responsible for displaying user information to an admin.
 * <p>
 * Allows the admin to:
 * <ul>
 *     <li>View user profile details (name, email, phone)</li>
 *     <li>Navigate to notifications the user has sent/received</li>
 *     <li>Perform a cascade delete that removes the user from all Firestore references</li>
 * </ul>
 *
 * This includes:
 * <ul>
 *     <li>Removing user from all event lists</li>
 *     <li>Deleting events created by the user</li>
 *     <li>Deleting notifications sent or received</li>
 *     <li>Deleting the user account document</li>
 * </ul>
 */
public class AdminUserDetailsFragment extends Fragment {

    private static final String TAG = "AdminUserDetails";
    private FragmentAdminUserDetailsBinding binding;
    private FirebaseFirestore db;
    private String userId;
    private String username;

    /**
     * Inflates the admin user details layout, loads data, initializes buttons,
     * and hides the admin bottom navigation bar.
     *
     * @param inflater  LayoutInflater to inflate the fragment UI
     * @param container Parent container the fragment UI will be attached to
     * @param savedInstanceState Saved instance state bundle
     * @return The root view of the inflated layout
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAdminUserDetailsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();

        // Retrieve userId passed from previous fragment
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }

        if (userId == null) {
            Toast.makeText(getContext(), "Error: No user selected", Toast.LENGTH_SHORT).show();
        } else {
            loadUserData(userId);
        }

        // Navigate to received notifications list
        binding.btnReceivedNotifications.setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), NotificationsReceivedActivity.class);
            i.putExtra("userId", userId);
            startActivity(i);
        });

        // Navigate to sent notifications list
        binding.btnSentNotifications.setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), NotificationsSentActivity.class);
            i.putExtra("userId", userId);
            startActivity(i);
        });

        // Back button returns to previous admin fragment
        binding.buttonBack.setOnClickListener(v ->
                Navigation.findNavController(requireActivity(),
                        R.id.nav_host_fragment_activity_admin).popBackStack()
        );

        // Cascade delete confirmation dialog
        binding.buttonRemove.setOnClickListener(v -> showDeleteConfirmation());

        // Hide bottom navigation bar while viewing user details
        if (getActivity() != null) {
            BottomNavigationView navView =
                    getActivity().findViewById(R.id.nav_view_admin);

            if (navView != null) {
                navView.setVisibility(View.GONE);
            }
        }

        return root;
    }

    /**
     * Loads the user's profile information from Firestore.
     *
     * @param userId ID of the user whose data will be fetched
     */
    private void loadUserData(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(this::setUserDetails)
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Failed to load data", Toast.LENGTH_SHORT).show());
    }

    /**
     * Populates UI fields with user information from Firestore.
     *
     * @param document Firestore user document snapshot
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
     * Displays a confirmation dialog informing the admin about the irreversible
     * cascade delete action before proceeding.
     */
    private void showDeleteConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Remove User Account")
                .setMessage("Are you sure you want to remove user '" + username + "'?\n\n" +
                        "This will:\n" +
                        "• Remove them from all events\n" +
                        "• Delete all events they created\n" +
                        "• Delete all notifications they sent/received\n" +
                        "• Permanently delete their account\n\n" +
                        "This action cannot be undone.")
                .setPositiveButton("Remove", (dialog, which) -> performCascadeDelete())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Initiates a full cascade delete operation which removes:
     * <ul>
     *     <li>User from all event participant lists</li>
     *     <li>All events created by the user</li>
     *     <li>All notifications sent by the user</li>
     *     <li>User from notifications they have received</li>
     *     <li>The user document itself</li>
     * </ul>
     *
     * This ensures no Firestore data remains referencing the deleted user.
     */
    private void performCascadeDelete() {
        if (userId == null) {
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

                        // Remove user from all event lists
                        if (waitList != null && waitList.remove(userId)) needsUpdate = true;
                        if (selectedList != null && selectedList.remove(userId)) needsUpdate = true;
                        if (finalList != null && finalList.remove(userId)) needsUpdate = true;
                        if (cancelledList != null && cancelledList.remove(userId)) needsUpdate = true;

                        if (needsUpdate) {
                            eventDoc.getReference()
                                    .update(
                                            "waitList", waitList,
                                            "selectedList", selectedList,
                                            "finalList", finalList,
                                            "cancelledList", cancelledList
                                    )
                                    .addOnSuccessListener(aVoid ->
                                            Log.d(TAG, "Removed user from event: " +
                                                    eventDoc.getString("eventName")))
                                    .addOnFailureListener(e ->
                                            Log.e(TAG, "Error removing user from event", e));
                        }
                    }

                    deleteEventsOwnedByUser();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error removing user from events: " + e.getMessage(), e);
                    Toast.makeText(requireContext(), "Error during account removal", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Queries and deletes all events where this user is the owner.
     * After completion, continues the cascade deletion process
     * by deleting notifications sent by the user.
     */
    private void deleteEventsOwnedByUser() {
        Log.d(TAG, "=== QUERYING EVENTS OWNED BY USER ===");
        Log.d(TAG, "Searching for ownerId: " + userId);

        db.collection("events")
                .whereEqualTo("ownerId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int eventCount = querySnapshot.size();
                    Log.d(TAG, "Found " + eventCount + " events owned by user: " + username);

                    for (QueryDocumentSnapshot eventDoc : querySnapshot) {
                        String eventId = eventDoc.getId();
                        String eventName = eventDoc.getString("eventName");

                        eventDoc.getReference()
                                .delete()
                                .addOnSuccessListener(aVoid ->
                                        Log.d(TAG, "Deleted event: " + eventName))
                                .addOnFailureListener(e ->
                                        Log.e(TAG, "Error deleting event: " + eventName, e));
                    }

                    deleteNotificationsSentByUser(eventCount);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed querying events owned by user", e);
                    deleteNotificationsSentByUser(0);
                });
    }

    /**
     * Deletes all notifications where this user is the sender.
     * Continues the cascade delete by removing the user from received notifications.
     *
     * @param eventCount Number of events deleted previously
     */
    private void deleteNotificationsSentByUser(int eventCount) {
        db.collection("notifications")
                .whereEqualTo("senderId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int sentCount = querySnapshot.size();
                    Log.d(TAG, "Found " + sentCount + " sent notifications.");

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        doc.getReference()
                                .delete()
                                .addOnSuccessListener(aVoid ->
                                        Log.d(TAG, "Deleted sent notification: " + doc.getId()))
                                .addOnFailureListener(e ->
                                        Log.e(TAG, "Error deleting sent notification", e));
                    }

                    deleteNotificationsReceivedByUser(eventCount, sentCount);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error querying sent notifications", e);
                    deleteNotificationsReceivedByUser(eventCount, 0);
                });
    }

    /**
     * Removes the user from the receivers list of all notifications they received.
     * If a notification has no receivers left after removal, the notification is deleted entirely.
     *
     * @param eventCount    Number of events deleted
     * @param sentCount     Number of sent notifications deleted
     */
    private void deleteNotificationsReceivedByUser(int eventCount, int sentCount) {
        db.collection("notifications")
                .whereArrayContains("receiversId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int receivedCount = querySnapshot.size();
                    Log.d(TAG, "Found " + receivedCount + " received notifications.");

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        List<String> receivers = (List<String>) doc.get("receiversId");

                        if (receivers != null) {
                            receivers.remove(userId);

                            if (receivers.isEmpty()) {
                                doc.getReference().delete()
                                        .addOnSuccessListener(aVoid ->
                                                Log.d(TAG, "Deleted notification (no receivers): "
                                                        + doc.getId()));
                            } else {
                                doc.getReference()
                                        .update("receiversId", receivers)
                                        .addOnSuccessListener(aVoid ->
                                                Log.d(TAG, "Updated receivers list: " + doc.getId()));
                            }
                        }
                    }

                    finalizeAccountDeletion(eventCount, sentCount, receivedCount);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error querying received notifications", e);
                    finalizeAccountDeletion(eventCount, sentCount, 0);
                });
    }

    /**
     * Deletes the user account document itself from Firestore.
     * Shows a summary report and navigates back to the previous admin screen.
     *
     * @param eventCount    Number of deleted events
     * @param sentCount     Number of deleted sent notifications
     * @param receivedCount Number of deleted received notifications
     */
    private void finalizeAccountDeletion(int eventCount, int sentCount, int receivedCount) {
        db.collection("users")
                .document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {

                    Log.d(TAG, "User deleted: " + username);

                    StringBuilder message = new StringBuilder(
                            "User " + username + " removed successfully."
                    );

                    if (eventCount > 0)
                        message.append("\n• ").append(eventCount).append(" event(s) deleted");
                    if (sentCount > 0)
                        message.append("\n• ").append(sentCount).append(" sent notification(s) deleted");
                    if (receivedCount > 0)
                        message.append("\n• ").append(receivedCount).append(" received notification(s) deleted");

                    Toast.makeText(requireContext(), message.toString(), Toast.LENGTH_LONG).show();

                    NavController navController =
                            Navigation.findNavController(requireActivity(),
                                    R.id.nav_host_fragment_activity_admin);

                    navController.popBackStack();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting user", e);
                    Toast.makeText(requireContext(),
                            "Error removing user account", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Restores the admin bottom navigation view when the fragment is destroyed.
     * Ensures UI consistency across admin screens.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (getActivity() != null) {
            BottomNavigationView navView =
                    getActivity().findViewById(R.id.nav_view_admin);

            if (navView != null) {
                navView.setVisibility(View.VISIBLE);
            }
        }

        binding = null;
    }
}

package com.example.lotterize.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.lotterize.R;
import com.example.lotterize.User;
import com.example.lotterize.databinding.FragmentAdminProfileBinding;
import com.example.lotterize.ui.addEvents.UsersRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays a list of all user profiles to the admin.
 * Allows searching for users and deleting user accounts with cascade deletion.
 */
public class AdminProfileFragment extends Fragment {

    static final String TAG = "AdminProfilesFragment";
    FragmentAdminProfileBinding binding;
    FirebaseFirestore db;
    UsersRepository usersRepository;
    List<UserProfile> allUsers = new ArrayList<>();

    /**
     * Simple class to hold user profile data.
     */
    public static class UserProfile {
        String userId;
        String username;

        public UserProfile(String userId, String username) {
            this.userId = userId;
            this.username = username;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentAdminProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();

        usersRepository = UsersRepository.getInstance();

        setupSearch();

        loadUserProfiles();

        return root;
    }

    /**
     * Sets up the search bar to filter users by username.
     */
    public void setupSearch() {
        binding.searchProfiles.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Filters the user list based on search query.
     *
     * @param query The search query entered by the admin
     */
    public void filterUsers(String query) {
        if (binding == null) return;

        binding.profilesContainer.removeAllViews();

        if (query.isEmpty()) {
            // Show all users if search is empty
            for (UserProfile user : allUsers) {
                addUserToView(user.userId, user.username);
            }

            binding.textEmptyState.setVisibility(
                    allUsers.isEmpty() ? View.VISIBLE : View.GONE
            );
        } else {
            // Filter users by username
            int count = 0;
            for (UserProfile user : allUsers) {
                if (user.username.toLowerCase().contains(query.toLowerCase())) {
                    addUserToView(user.userId, user.username);
                    count++;
                }
            }

            // Show empty state if no matches
            if (count == 0) {
                binding.textEmptyState.setVisibility(View.VISIBLE);
            } else {
                binding.textEmptyState.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Retrieves all users using UsersRepository and displays their usernames.
     * This is test-friendly because UsersRepository can be replaced with a mock.
     */
    public void loadUserProfiles() {
        Log.d(TAG, "Loading user profiles from UsersRepository...");

        usersRepository.fetchAllUsers(new UsersRepository.UsersCallback() {
            @Override
            public void onSuccess(@NonNull ArrayList<User> users) {
                if (binding == null) return;

                binding.profilesContainer.removeAllViews();
                allUsers.clear();

                for (User user : users) {
                    String displayName;

                    // Prefer username if present, then name, then raw ID
                    if (user.getUsername() != null && !user.getUsername().isEmpty()) {
                        displayName = user.getUsername();
                    } else if (user.getName() != null && !user.getName().isEmpty()) {
                        displayName = user.getName();
                    } else {
                        displayName = user.getUserId();
                    }

                    allUsers.add(new UserProfile(user.getUserId(), displayName));
                    addUserToView(user.getUserId(), displayName);
                    Log.d(TAG, "Loaded user: " + displayName);
                }

                if (allUsers.isEmpty()) {
                    binding.textEmptyState.setVisibility(View.VISIBLE);
                    Log.d(TAG, "No users found");
                } else {
                    binding.textEmptyState.setVisibility(View.GONE);
                }

                Log.d(TAG, "Total users loaded: " + allUsers.size());
            }

            @Override
            public void onError(@NonNull Exception e) {
                Log.e(TAG, "Error loading users from UsersRepository: " + e.getMessage(), e);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error loading users", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Dynamically adds a single user profile to the scrollable view.
     *
     * @param userId   The user's document ID in Firestore
     * @param username The user's username/display name
     */
    public void addUserToView(String userId, String username) {
        if (binding == null) return;

        // Create a horizontal layout for each user row
        LinearLayout userItem = new LinearLayout(getContext());
        userItem.setOrientation(LinearLayout.HORIZONTAL);
        userItem.setPadding(0, 16, 0, 16);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        userItem.setLayoutParams(params);

        // Username (left side)
        TextView usernameView = new TextView(getContext());
        usernameView.setText(username);
        usernameView.setTextColor(0xFF000000);
        usernameView.setTextSize(18);
        usernameView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        usernameView.setLayoutParams(nameParams);

        // Delete button (right side)
        Button deleteButton = new Button(getContext());
        deleteButton.setText("Delete");
        deleteButton.setTextColor(0xFFFFFFFF);
        deleteButton.setBackgroundColor(0xFFDC143C);
        deleteButton.setTextSize(14);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        deleteButton.setLayoutParams(buttonParams);

        // Delete button click listener
        deleteButton.setOnClickListener(v -> showDeleteConfirmation(userId, username));

        userItem.addView(usernameView);
        userItem.addView(deleteButton);

        // Divider line between users
        View divider = new View(getContext());
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1
        );
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(0xFFE0E0E0);

        binding.profilesContainer.addView(userItem);
        binding.profilesContainer.addView(divider);

        usernameView.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            Bundle bundle = new Bundle();
            bundle.putString("userId", userId);
            navController.navigate(
                    R.id.action_navigation_admin_profile_to_navigation_admin_user_details,
                    bundle
            );
        });
    }

    /**
     * Shows a confirmation dialog before deleting a user.
     *
     * @param userId   The user's document ID
     * @param username The user's username
     */
    public void showDeleteConfirmation(String userId, String username) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete user '" + username + "'?\n\n" +
                        "This will:\n" +
                        "• Remove them from all events\n" +
                        "• Delete all events they created\n" +
                        "• Delete all notifications they sent/received\n" +
                        "• Permanently delete their account\n\n" +
                        "This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteUser(userId, username))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Performs cascade delete: removes user from events, deletes their events,
     * deletes their notifications (sent and received), then deletes the user account.
     *
     * @param userId   The user's document ID
     * @param username The user's username (for logging)
     */
    public void deleteUser(String userId, String username) {
        Log.d(TAG, "Starting cascade delete for user: " + username);

        // Remove user from all events (waitList, selectedList, etc.)
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
                                            Log.d(TAG, "Removed user from event: " +
                                                    eventDoc.getString("eventName")))
                                    .addOnFailureListener(e ->
                                            Log.e(TAG, "Error removing user from event", e));
                        }
                    }

                    // Delete events owned by this user
                    deleteEventsOwnedByUser(userId, username);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error removing user from events: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "Error during cascade delete", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Deletes all events created by the specified user.
     *
     * @param userId   The user's document ID
     * @param username The user's username (for logging)
     */
    public void deleteEventsOwnedByUser(String userId, String username) {
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

                    // Delete notifications sent by this user
                    deleteNotificationsSentByUser(userId, username, eventCount);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error finding user's events: " + e.getMessage(), e);
                    deleteNotificationsSentByUser(userId, username, 0);
                });
    }

    /**
     * Deletes all notifications sent by the user.
     *
     * @param userId     The user's document ID
     * @param username   The user's username (for logging)
     * @param eventCount Number of events deleted
     */
    public void deleteNotificationsSentByUser(String userId, String username, int eventCount) {
        db.collection("notifications")
                .whereEqualTo("senderId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int sentCount = querySnapshot.size();
                    Log.d(TAG, "Found " + sentCount + " notifications sent by user: " + username);

                    // Delete each notification sent by the user
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        doc.getReference()
                                .delete()
                                .addOnSuccessListener(aVoid ->
                                        Log.d(TAG, "Deleted sent notification: " + doc.getId()))
                                .addOnFailureListener(e ->
                                        Log.e(TAG, "Error deleting sent notification", e));
                    }

                    // Delete notifications received by this user
                    deleteNotificationsReceivedByUser(userId, username, eventCount, sentCount);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error finding sent notifications: " + e.getMessage(), e);
                    deleteNotificationsReceivedByUser(userId, username, eventCount, 0);
                });
    }

    /**
     * Removes user from notifications they received
     * (removes userId from receiversId array, or deletes notification if they're the only receiver).
     *
     * @param userId        The user's document ID
     * @param username      The user's username (for logging)
     * @param eventCount    Number of events deleted
     * @param sentCount     Number of sent notifications deleted
     */
    public void deleteNotificationsReceivedByUser(String userId, String username,
                                                  int eventCount, int sentCount) {
        db.collection("notifications")
                .whereArrayContains("receiversId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int receivedCount = querySnapshot.size();
                    Log.d(TAG, "Found " + receivedCount + " notifications received by user: " + username);

                    // Remove user from each notification's receiversId array
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        List<String> receiversId = (List<String>) doc.get("receiversId");

                        if (receiversId != null) {
                            receiversId.remove(userId);

                            if (receiversId.isEmpty()) {
                                // If no more receivers, delete the entire notification
                                doc.getReference()
                                        .delete()
                                        .addOnSuccessListener(aVoid ->
                                                Log.d(TAG, "Deleted notification (no more receivers): " + doc.getId()))
                                        .addOnFailureListener(e ->
                                                Log.e(TAG, "Error deleting notification", e));
                            } else {
                                // Otherwise, just update the receiversId array
                                doc.getReference()
                                        .update("receiversId", receiversId)
                                        .addOnSuccessListener(aVoid ->
                                                Log.d(TAG, "Removed user from notification: " + doc.getId()))
                                        .addOnFailureListener(e ->
                                                Log.e(TAG, "Error updating notification", e));
                            }
                        }
                    }

                    // Finally delete the user document
                    deleteUserDocument(userId, username, eventCount, sentCount, receivedCount);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error finding received notifications: " + e.getMessage(), e);
                    // Still try to delete the user even if this fails
                    deleteUserDocument(userId, username, eventCount, sentCount, 0);
                });
    }

    /**
     * Deletes the user's document from Firestore.
     *
     * @param userId        The user's document ID
     * @param username      The user's username (for logging)
     * @param eventCount    Number of events deleted
     * @param sentCount     Number of sent notifications deleted
     * @param receivedCount Number of received notifications deleted
     */
    public void deleteUserDocument(String userId, String username,
                                   int eventCount, int sentCount, int receivedCount) {
        db.collection("users")
                .document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User deleted: " + username);

                    // Build detailed success message
                    StringBuilder message = new StringBuilder("User " + username + " deleted successfully.");
                    if (eventCount > 0) {
                        message.append("\n• ").append(eventCount).append(" event(s) deleted");
                    }
                    if (sentCount > 0) {
                        message.append("\n• ").append(sentCount).append(" sent notification(s) deleted");
                    }
                    if (receivedCount > 0) {
                        message.append("\n• ").append(receivedCount).append(" received notification(s) deleted");
                    }

                    if (getContext() != null) {
                        Toast.makeText(getContext(), message.toString(), Toast.LENGTH_LONG).show();
                    }

                    // Remove from list and refresh view
                    allUsers.removeIf(user -> user.userId.equals(userId));
                    if (binding != null) {
                        filterUsers(binding.searchProfiles.getText().toString());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting user: " + e.getMessage(), e);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error deleting user", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        BottomNavigationView navView = getActivity().findViewById(R.id.nav_view_admin);
        if (navView != null) navView.setVisibility(View.VISIBLE);

        // Refresh user list when returning to this fragment
        loadUserProfiles();
    }
}
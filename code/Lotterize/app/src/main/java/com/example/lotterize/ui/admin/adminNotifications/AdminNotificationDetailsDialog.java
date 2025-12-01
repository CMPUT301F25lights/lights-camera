package com.example.lotterize.ui.admin.adminNotifications;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.lotterize.Notification;
import com.example.lotterize.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * DialogFragment that shows all details for a single notification in the admin log.
 * Fetches usernames from Firestore for receiver IDs to display human-readable names.
 */
public class AdminNotificationDetailsDialog extends DialogFragment {

    private static final String TAG = "AdminNotifDetails";
    private static final String ARG_NOTIFICATION = "arg_notification";
    private final SimpleDateFormat tsFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    private FirebaseFirestore db;
    private TextView receiversTextView;
    private ArrayList<String> receiverIds;

    /**
     * Empty public constructor required for fragments.
     */
    public AdminNotificationDetailsDialog() {}

    /**
     * Creates a new instance of this dialog for the given notification.
     */
    public static AdminNotificationDetailsDialog newInstance(Notification notification) {
        AdminNotificationDetailsDialog f = new AdminNotificationDetailsDialog();
        Bundle args = new Bundle();

        args.putString("notificationId", notification.getNotificationId());
        args.putString("senderName", notification.getSenderName());
        args.putString("senderId", notification.getSenderId());
        args.putString("message", notification.getMessage());

        if (notification.getTime() != null) {
            args.putLong("time", notification.getTime().toDate().getTime());
        }

        args.putStringArrayList("receivers", new ArrayList<>(notification.getReceiversId()));

        f.setArguments(args);
        return f;
    }

    @SuppressLint("SetTextI18n")
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        db = FirebaseFirestore.getInstance();

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_admin_notification_details, null);

        TextView notificationIdTextView = view.findViewById(R.id.text_notification_id);
        TextView senderTextView = view.findViewById(R.id.text_sender);
        TextView timeTextView = view.findViewById(R.id.text_time);
        TextView msgTextView = view.findViewById(R.id.text_message);
        TextView receiversCountTextView = view.findViewById(R.id.text_receivers_count);
        receiversTextView = view.findViewById(R.id.text_receivers);

        Bundle args = getArguments();

        String notificationId = args != null ? args.getString("notificationId") : null;
        String senderId = args != null ? args.getString("senderId") : null;
        String message = args != null ? args.getString("message") : null;
        long time = args != null ? args.getLong("time", -1L) : -1L;
        receiverIds = args != null ? args.getStringArrayList("receivers") : null;

        if (notificationId == null || notificationId.isEmpty()) {
            notificationId = "(No id)";
        }
        notificationIdTextView.setText(notificationId);

        // Fetch actual sender username from Firestore instead of trusting stored senderName
        if (senderId != null && !senderId.isEmpty()) {
            senderTextView.setText("Loading sender info...");
            fetchSenderUsername(senderId, senderTextView);
        } else {
            senderTextView.setText("Unknown sender (No ID)");
        }

        if (time > 0) {
            String formatted = tsFormat.format(new java.util.Date(time));
            timeTextView.setText(formatted);
        } else {
            timeTextView.setText("(No timestamp)");
        }

        if (message == null || message.isEmpty()) {
            message = "(No message)";
        }
        msgTextView.setText(message);

        int size = (receiverIds != null) ? receiverIds.size() : 0;
        receiversCountTextView.setText(String.valueOf(size));

        // Show loading state while fetching usernames
        receiversTextView.setText("Loading receiver names...");

        // Fetch usernames for all receiver IDs
        if (receiverIds != null && !receiverIds.isEmpty()) {
            fetchReceiverUsernames(receiverIds);
        } else {
            receiversTextView.setText("No receivers");
        }

        return new AlertDialog.Builder(requireContext())
                .setView(view)
                .setTitle("Notification details")
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                .create();
    }

    /**
     * Fetches the actual sender username from Firestore based on senderId.
     * This ensures we show the correct username even if the stored senderName is wrong.
     *
     * @param senderId The user ID of the sender
     * @param senderTextView The TextView to update with sender info
     */
    private void fetchSenderUsername(String senderId, TextView senderTextView) {
        db.collection("users")
                .document(senderId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String username = document.getString("username");
                        if (username != null && !username.isEmpty()) {
                            senderTextView.setText(username + " (ID: " + senderId + ")");
                        } else {
                            senderTextView.setText("Unknown (ID: " + senderId + ")");
                        }
                    } else {
                        senderTextView.setText("[Deleted User] (ID: " + senderId + ")");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching sender username for " + senderId, e);
                    senderTextView.setText("[Error loading] (ID: " + senderId + ")");
                });
    }

    /**
     * Fetches usernames for all receiver IDs from Firestore and displays them.
     *
     * @param receiverIds List of user IDs to fetch usernames for
     */
    private void fetchReceiverUsernames(ArrayList<String> receiverIds) {
        Map<String, String> userIdToUsername = new HashMap<>();
        final int[] fetchedCount = {0};
        final int totalCount = receiverIds.size();

        for (String userId : receiverIds) {
            db.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(document -> {
                        fetchedCount[0]++;

                        if (document.exists()) {
                            String username = document.getString("username");
                            if (username != null && !username.isEmpty()) {
                                userIdToUsername.put(userId, username);
                            } else {
                                userIdToUsername.put(userId, "Unknown");
                            }
                        } else {
                            // User document not found (deleted account)
                            userIdToUsername.put(userId, "[Deleted User]");
                        }

                        // When all usernames are fetched, update the view
                        if (fetchedCount[0] == totalCount) {
                            displayReceivers(receiverIds, userIdToUsername);
                        }
                    })
                    .addOnFailureListener(e -> {
                        fetchedCount[0]++;
                        Log.e(TAG, "Error fetching username for " + userId, e);
                        userIdToUsername.put(userId, "[Error loading]");

                        // Even on failure, continue when all are processed
                        if (fetchedCount[0] == totalCount) {
                            displayReceivers(receiverIds, userIdToUsername);
                        }
                    });
        }
    }

    /**
     * Displays receivers with their usernames and IDs in a readable format.
     *
     * @param receiverIds List of receiver user IDs
     * @param userIdToUsername Map of userId to username
     */
    private void displayReceivers(ArrayList<String> receiverIds, Map<String, String> userIdToUsername) {
        StringBuilder sb = new StringBuilder();

        for (String userId : receiverIds) {
            String username = userIdToUsername.getOrDefault(userId, "Unknown");
            sb.append("• ").append(username).append("\n");
            sb.append("  (ID: ").append(userId).append(")\n\n");
        }

        // Remove trailing newlines
        String result = sb.toString().trim();
        receiversTextView.setText(result);

        Log.d(TAG, "Displayed " + receiverIds.size() + " receivers with usernames");
    }
}
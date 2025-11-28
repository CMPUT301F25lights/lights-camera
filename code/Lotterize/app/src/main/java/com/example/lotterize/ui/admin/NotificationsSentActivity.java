package com.example.lotterize.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lotterize.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.List;

/**
 * Activity that displays all notifications sent by a specific user.
 * Queries Firestore for notifications where senderId matches the user.
 */
public class NotificationsSentActivity extends AppCompatActivity {

    private static final String TAG = "NotificationsSent";
    private FirebaseFirestore db;
    private String userId;
    private LinearLayout notificationsContainer;
    private TextView emptyStateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_sent);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get userId from intent
        userId = getIntent().getStringExtra("userId");

        if (userId == null) {
            Toast.makeText(this, "Error: No user ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        notificationsContainer = findViewById(R.id.notifications_container);
        emptyStateText = findViewById(R.id.text_empty_state);

        // Back button
        View backButton = findViewById(R.id.buttonBack);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Load notifications sent by this user
        loadSentNotifications();
    }

    /**
     * Queries Firestore for all notifications where senderId matches the current user.
     */
    private void loadSentNotifications() {
        Log.d(TAG, "Loading notifications sent by user: " + userId);

        db.collection("notifications")
                .whereEqualTo("senderId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    notificationsContainer.removeAllViews();

                    if (querySnapshot.isEmpty()) {
                        // Show empty state
                        if (emptyStateText != null) {
                            emptyStateText.setVisibility(View.VISIBLE);
                            emptyStateText.setText("No notifications sent");
                        }
                        Log.d(TAG, "No sent notifications found for user");
                    } else {
                        // Hide empty state
                        if (emptyStateText != null) {
                            emptyStateText.setVisibility(View.GONE);
                        }

                        // Add each notification to the view
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            String notificationId = doc.getId();
                            String message = doc.getString("message");
                            // FIXED: receiversId is an ArrayList, not a String
                            List<String> receiversIdList = (List<String>) doc.get("receiversId");
                            String senderName = doc.getString("senderName");

                            addNotificationToView(notificationId, message, receiversIdList);
                            Log.d(TAG, "Loaded notification: " + notificationId + " - " + message);
                        }

                        Log.d(TAG, "Total sent notifications loaded: " + querySnapshot.size());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading sent notifications: " + e.getMessage(), e);
                    Toast.makeText(this, "Failed to load notifications", Toast.LENGTH_SHORT).show();

                    if (emptyStateText != null) {
                        emptyStateText.setVisibility(View.VISIBLE);
                        emptyStateText.setText("Error loading notifications");
                    }
                });
    }

    /**
     * Dynamically creates and adds a notification card to the view.
     *
     * @param notificationId The notification document ID
     * @param message The notification message
     * @param receiversIdList The list of user IDs who received the notification
     */
    private void addNotificationToView(String notificationId, String message, List<String> receiversIdList) {
        // Create container for notification
        LinearLayout notificationItem = new LinearLayout(this);
        notificationItem.setOrientation(LinearLayout.VERTICAL);
        notificationItem.setPadding(40, 32, 40, 32);
        notificationItem.setBackgroundColor(0xFFFFFFFF);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        notificationItem.setLayoutParams(params);

        // Receivers - display count and list
        TextView receiverView = new TextView(this);
        if (receiversIdList != null && !receiversIdList.isEmpty()) {
            if (receiversIdList.size() == 1) {
                // Single recipient - show their ID
                receiverView.setText("To: " + receiversIdList.get(0));
            } else {
                // Multiple recipients - show count
                receiverView.setText("To: " + receiversIdList.size() + " recipients");
            }
        } else {
            receiverView.setText("To: Unknown receivers");
        }
        receiverView.setTextSize(14);
        receiverView.setTextColor(0xFF666666);
        receiverView.setPadding(0, 0, 0, 8);
        notificationItem.addView(receiverView);

        // Message (main content)
        TextView messageView = new TextView(this);
        messageView.setText(message != null ? message : "No message");
        messageView.setTextSize(16);
        messageView.setTextColor(0xFF000000);
        messageView.setPadding(0, 0, 0, 8);
        messageView.setLineSpacing(0, 1.2f);
        notificationItem.addView(messageView);

        // Add a thin divider line
        View divider = new View(this);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2
        );
        dividerParams.setMargins(0, 16, 0, 0);
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(0xFFE0E0E0); // Light gray

        notificationsContainer.addView(notificationItem);
        notificationsContainer.addView(divider);

    }

}
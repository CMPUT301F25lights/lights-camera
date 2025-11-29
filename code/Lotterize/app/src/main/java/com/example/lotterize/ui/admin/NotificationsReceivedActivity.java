package com.example.lotterize.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lotterize.Notification;
import com.example.lotterize.R;
import com.example.lotterize.ui.admin.adminNotifications.AdminNotificationDetailsDialog;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NotificationsReceivedActivity extends AppCompatActivity {

    private static final String TAG = "NotificationsReceived";
    private FirebaseFirestore db;
    private String userId;
    private LinearLayout notificationsContainer;
    private TextView emptyStateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_received);

        db = FirebaseFirestore.getInstance();

        // Get userId from intent
        userId = getIntent().getStringExtra("userId");

        if (userId == null) {
            Toast.makeText(this, "Error: No user ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Loading notifications for user: " + userId);

        // Initialize views
        notificationsContainer = findViewById(R.id.notifications_container);
        emptyStateText = findViewById(R.id.text_empty_state);

        // Back button
        View backButton = findViewById(R.id.buttonBack);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Load notifications
        loadReceivedNotifications();
    }

    private void loadReceivedNotifications() {
        Log.d(TAG, "Querying notifications where receiversId array contains: " + userId);

        // USE arrayContains BECAUSE receiversId IS AN ARRAY
        db.collection("notifications")
                .whereArrayContains("receiversId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    notificationsContainer.removeAllViews();

                    if (querySnapshot.isEmpty()) {
                        if (emptyStateText != null) {
                            emptyStateText.setVisibility(View.VISIBLE);
                            emptyStateText.setText("No notifications received");
                        }
                        Log.d(TAG, "No notifications found");
                    } else {
                        if (emptyStateText != null) {
                            emptyStateText.setVisibility(View.GONE);
                        }

                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            String notificationId = doc.getString("notificationId");
                            String message = doc.getString("message");
                            String senderName = doc.getString("senderName");
                            String senderId = doc.getString("senderId");
                            Timestamp timestamp = doc.getTimestamp("time");
                            ArrayList<String> receiversId = (ArrayList<String>) doc.get("receiversId");

                            // Create Notification object for the dialog
                            Notification notification = new Notification(
                                    notificationId,
                                    senderId,
                                    senderName,
                                    message,
                                    timestamp,
                                    receiversId != null ? receiversId : new ArrayList<>()
                            );

                            addNotificationToView(message, notification);
                            Log.d(TAG, "Loaded notification: " + message);
                        }

                        Log.d(TAG, "Total notifications loaded: " + querySnapshot.size());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading notifications: " + e.getMessage(), e);
                    Toast.makeText(this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
                    if (emptyStateText != null) {
                        emptyStateText.setVisibility(View.VISIBLE);
                        emptyStateText.setText("Error loading notifications");
                    }
                });
    }

    private void addNotificationToView(String message, Notification notification) {
        // Create container for notification
        LinearLayout notificationItem = new LinearLayout(this);
        notificationItem.setOrientation(LinearLayout.VERTICAL);
        notificationItem.setPadding(40, 32, 40, 32);
        notificationItem.setBackgroundColor(0xFFFFFFFF); // White background
        notificationItem.setClickable(true);
        notificationItem.setFocusable(true);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        notificationItem.setLayoutParams(params);

        // Message (main content - larger and bold)
        TextView messageView = new TextView(this);
        messageView.setText(message != null ? message : "No message");
        messageView.setTextSize(16);
        messageView.setTextColor(0xFF000000);
        messageView.setLineSpacing(4, 1.0f);
        notificationItem.addView(messageView);

        // Set click listener to show details dialog
        notificationItem.setOnClickListener(v -> {
            AdminNotificationDetailsDialog dialog = AdminNotificationDetailsDialog.newInstance(notification);
            dialog.show(getSupportFragmentManager(), "notification_details");
        });

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
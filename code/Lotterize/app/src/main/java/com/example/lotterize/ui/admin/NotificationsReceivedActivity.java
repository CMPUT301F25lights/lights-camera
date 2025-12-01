package com.example.lotterize.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lotterize.Notification;
import com.example.lotterize.NotificationsRepository;
import com.example.lotterize.R;
import com.example.lotterize.ui.admin.adminNotifications.AdminNotificationDetailsDialog;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * Activity responsible for displaying all notifications received by a specific user.
 * <p>
 * This activity loads notifications from Firestore where the user's ID appears in the
 * {@code receiversId} array field. Each notification is displayed as a clickable view
 * that opens a dialog showing detailed information.
 */
public class NotificationsReceivedActivity extends AppCompatActivity {

    private static final String TAG = "NotificationsReceived";
    private String userId;
    private NotificationsRepository notificationsRepository;
    private LinearLayout notificationsContainer;
    private TextView emptyStateText;

    /**
     * Called when the activity is created.
     * Initializes the UI, retrieves the user ID, and triggers loading of notifications.
     *
     * @param savedInstanceState Previously saved activity state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_received);

        notificationsRepository = NotificationsRepository.getInstance();

        // Retrieve userId passed from the previous activity
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

        // Load all notifications for this user
        loadReceivedNotifications();
    }

    /**
     * Loads notifications from Firestore where the {@code receiversId} array
     * contains the user ID. Updates the UI based on results.
     */
    private void loadReceivedNotifications() {
        Log.d(TAG, "Loading received notifications via repository for user: " + userId);

        notificationsRepository.fetchNotificationsReceived(
                userId,
                new NotificationsRepository.NotificationsCallback() {
                    @Override
                    public void onSuccess(@NonNull ArrayList<Notification> notifications) {
                        notificationsContainer.removeAllViews();

                        if (notifications.isEmpty()) {
                            if (emptyStateText != null) {
                                emptyStateText.setVisibility(View.VISIBLE);
                                emptyStateText.setText("No notifications received");
                            }
                            Log.d(TAG, "No notifications found");
                        } else {
                            if (emptyStateText != null) {
                                emptyStateText.setVisibility(View.GONE);
                            }

                            for (Notification notification : notifications) {
                                addNotificationToView(notification.getMessage(), notification);
                                Log.d(TAG, "Loaded notification: " + notification.getMessage());
                            }

                            Log.d(TAG, "Total notifications loaded: " + notifications.size());
                        }
                    }

                    @Override
                    public void onError(@NonNull Exception e) {
                        Log.e(TAG, "Error loading notifications", e);
                        Toast.makeText(NotificationsReceivedActivity.this,
                                "Failed to load notifications", Toast.LENGTH_SHORT).show();

                        if (emptyStateText != null) {
                            emptyStateText.setVisibility(View.VISIBLE);
                            emptyStateText.setText("Error loading notifications");
                        }
                    }
                }
        );
    }


    /**
     * Creates a styled view for a single notification and adds it to the container.
     * When clicked, it shows the {@link AdminNotificationDetailsDialog} dialog.
     *
     * @param message      Notification message text.
     * @param notification The full notification object containing details.
     */
    private void addNotificationToView(String message, Notification notification) {

        // Create container for a single notification item
        LinearLayout notificationItem = new LinearLayout(this);
        notificationItem.setOrientation(LinearLayout.VERTICAL);
        notificationItem.setPadding(40, 32, 40, 32);
        notificationItem.setBackgroundColor(0xFFFFFFFF);
        notificationItem.setClickable(true);
        notificationItem.setFocusable(true);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        notificationItem.setLayoutParams(params);

        // Create and style the message text
        TextView messageView = new TextView(this);
        messageView.setText(message != null ? message : "No message");
        messageView.setTextSize(16);
        messageView.setTextColor(0xFF000000);
        messageView.setLineSpacing(4, 1.0f);
        notificationItem.addView(messageView);

        // Click opens details dialog
        notificationItem.setOnClickListener(v -> {
            AdminNotificationDetailsDialog dialog =
                    AdminNotificationDetailsDialog.newInstance(notification);
            dialog.show(getSupportFragmentManager(), "notification_details");
        });

        // Divider line
        View divider = new View(this);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2
        );
        dividerParams.setMargins(0, 16, 0, 0);
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(0xFFE0E0E0);

        notificationsContainer.addView(notificationItem);
        notificationsContainer.addView(divider);
    }
}

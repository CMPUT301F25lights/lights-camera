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
 * Activity that displays all notifications sent by a specific user.
 * <p>
 * This activity queries Firestore for documents in the {@code notifications} collection
 * where the {@code senderId} field matches the provided user ID. Each notification
 * is displayed in a scrollable list, and tapping on one shows a detail dialog.
 */
public class NotificationsSentActivity extends AppCompatActivity {

    private static final String TAG = "NotificationsSent";
    private String userId;
    private NotificationsRepository notificationsRepository;
    private LinearLayout notificationsContainer;
    private TextView emptyStateText;

    /**
     * Called when the activity is created.
     * <p>
     * Sets up the UI, retrieves the user ID from the launching intent, configures
     * the back button, and begins loading all notifications sent by this user.
     *
     * @param savedInstanceState Saved activity state, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_sent);

        notificationsRepository = NotificationsRepository.getInstance();

        // Retrieve userId passed via intent
        userId = getIntent().getStringExtra("userId");

        if (userId == null) {
            Toast.makeText(this, "Error: No user ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Loading notifications sent by user: " + userId);

        // Initialize UI components
        notificationsContainer = findViewById(R.id.notifications_container);
        emptyStateText = findViewById(R.id.text_empty_state);

        // Back button
        View backButton = findViewById(R.id.buttonBack);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Load the notifications sent by the user
        loadSentNotifications();
    }

    /**
     * Loads all notifications from Firestore where {@code senderId} equals the
     * current user's ID. Results are displayed dynamically in the container layout.
     */
    private void loadSentNotifications() {
        Log.d(TAG, "Querying notifications (repository) where senderId equals: " + userId);

        notificationsRepository.fetchNotificationsSent(
                userId,
                new NotificationsRepository.NotificationsCallback() {
                    @Override
                    public void onSuccess(@NonNull ArrayList<Notification> notifications) {
                        notificationsContainer.removeAllViews();

                        if (notifications.isEmpty()) {
                            if (emptyStateText != null) {
                                emptyStateText.setVisibility(View.VISIBLE);
                                emptyStateText.setText("No notifications sent");
                            }
                            Log.d(TAG, "No sent notifications found");
                        } else {
                            if (emptyStateText != null) {
                                emptyStateText.setVisibility(View.GONE);
                            }

                            for (Notification notification : notifications) {
                                String message = notification.getMessage();
                                addNotificationToView(message, notification);
                                Log.d(TAG, "Loaded sent notification: " + message);
                            }

                            Log.d(TAG, "Total sent notifications loaded: " + notifications.size());
                        }
                    }

                    @Override
                    public void onError(@NonNull Exception e) {
                        Log.e(TAG, "Error loading sent notifications", e);
                        Toast.makeText(NotificationsSentActivity.this,
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
     * Creates a styled UI card to represent a single sent notification and adds it
     * into the notifications list. When clicked, the admin notification detail dialog opens.
     *
     * @param message      The main notification message to display.
     * @param notification The full notification object used for dialog details.
     */
    private void addNotificationToView(String message, Notification notification) {

        // Create container for individual notification card
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

        // Add message text
        TextView messageView = new TextView(this);
        messageView.setText(message != null ? message : "No message");
        messageView.setTextSize(16);
        messageView.setTextColor(0xFF000000);
        messageView.setLineSpacing(4, 1.0f);
        notificationItem.addView(messageView);

        // Clicking the card shows full details dialog
        notificationItem.setOnClickListener(v -> {
            AdminNotificationDetailsDialog dialog =
                    AdminNotificationDetailsDialog.newInstance(notification);
            dialog.show(getSupportFragmentManager(), "notification_details");
        });

        // Divider line underneath
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

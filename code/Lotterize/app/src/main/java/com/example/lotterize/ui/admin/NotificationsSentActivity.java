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

import java.util.ArrayList;

/**
 * Activity that displays all notifications sent by a specific user.
 *
 * <p>This activity retrieves all {@link Notification} objects whose
 * {@code senderId} matches the ID passed through the launching {@link android.content.Intent}.
 * The notifications are displayed in a vertical list, and each entry can be tapped
 * to open an {@link com.example.lotterize.ui.admin.adminNotifications.AdminNotificationDetailsDialog}
 * containing the full message details.</p>
 *
 * <p>If no notifications are found, the activity shows an empty-state message instead.</p>
 */
public class NotificationsSentActivity extends AppCompatActivity {

    private static final String TAG = "NotificationsSent";
    private String userId;
    private NotificationsRepository notificationsRepository;
    private LinearLayout notificationsContainer;
    private TextView emptyStateText;

    /**
     * Called when the activity is created.
     *
     * <p>This method initializes the UI layout, retrieves the user ID from the
     * incoming intent, configures the back button, and begins loading all
     * notifications sent by the specified user.</p>
     *
     * @param savedInstanceState Previously saved state, or {@code null} if none exists.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_sent);

        notificationsRepository = NotificationsRepository.getInstance();
        userId = getIntent().getStringExtra("userId");

        if (userId == null) {
            Toast.makeText(this, "Error: No user ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Loading notifications sent by user: " + userId);

        // Initialize layout components
        notificationsContainer = findViewById(R.id.notifications_container);
        emptyStateText = findViewById(R.id.text_empty_state);

        // Configure back button behavior
        View backButton = findViewById(R.id.buttonBack);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Start loading notifications for this user
        loadSentNotifications();
    }

    /**
     * Loads all notifications from Firestore where the {@code senderId} field
     * equals the currently active user's ID.
     *
     * <p>The results are provided asynchronously via the
     * {@link NotificationsRepository.NotificationsCallback}. When successful,
     * each notification is rendered into the container layout; if none are found,
     * an empty-state message is displayed.</p>
     */
    private void loadSentNotifications() {
        Log.d(TAG, "Querying notifications (repository) where senderId equals: " + userId);

        notificationsRepository.fetchNotificationsSent(
                userId,
                new NotificationsRepository.NotificationsCallback() {

                    /**
                     * Called when all sent notifications have been successfully retrieved.
                     *
                     * @param notifications A list of notifications sent by the current user.
                     */
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

                    /**
                     * Called when an error occurs while fetching sent notifications.
                     *
                     * @param e The exception describing the cause of the failure.
                     */
                    @Override
                    public void onError(@NonNull Exception e) {
                        Log.e(TAG, "Error loading sent notifications", e);
                        Toast.makeText(
                                NotificationsSentActivity.this,
                                "Failed to load notifications",
                                Toast.LENGTH_SHORT
                        ).show();

                        if (emptyStateText != null) {
                            emptyStateText.setVisibility(View.VISIBLE);
                            emptyStateText.setText("Error loading notifications");
                        }
                    }
                }
        );
    }

    /**
     * Creates a styled UI card representing a single sent notification and
     * inserts it into the notifications list.
     *
     * <p>The generated layout includes the primary message text and is fully
     * clickable. Tapping it opens a dialog that displays all details of the
     * notification, using {@link AdminNotificationDetailsDialog}.</p>
     *
     * @param message      The primary message text to display in the notification card.
     * @param notification The full {@link Notification} object, passed to the details dialog.
     */
    private void addNotificationToView(String message, Notification notification) {

        // Card container for the notification
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

        // Add message content
        TextView messageView = new TextView(this);
        messageView.setText(message != null ? message : "No message");
        messageView.setTextSize(16);
        messageView.setTextColor(0xFF000000);
        messageView.setLineSpacing(4, 1.0f);
        notificationItem.addView(messageView);

        // Open details dialog when clicked
        notificationItem.setOnClickListener(v -> {
            AdminNotificationDetailsDialog dialog =
                    AdminNotificationDetailsDialog.newInstance(notification);
            dialog.show(getSupportFragmentManager(), "notification_details");
        });

        // Divider line beneath the card
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

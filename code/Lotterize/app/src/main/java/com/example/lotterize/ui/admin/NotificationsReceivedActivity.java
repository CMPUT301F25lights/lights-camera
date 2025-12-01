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
 * Activity responsible for displaying all notifications received by a specific user.
 *
 * <p>This activity queries Firestore (via {@link NotificationsRepository}) for
 * all {@link Notification} objects in which the user's ID is present in the
 * {@code receiversId} array field. The results are displayed in a scrollable list,
 * and tapping a notification opens a detailed dialog using
 * {@link com.example.lotterize.ui.admin.adminNotifications.AdminNotificationDetailsDialog}.</p>
 *
 * <p>If no notifications are found, an empty state message is shown instead.</p>
 */
public class NotificationsReceivedActivity extends AppCompatActivity {

    private static final String TAG = "NotificationsReceived";
    private String userId;
    private NotificationsRepository notificationsRepository;
    private LinearLayout notificationsContainer;
    private TextView emptyStateText;

    /**
     * Called when the activity is first created.
     *
     * <p>This method initializes UI components, retrieves the user ID passed
     * through the launching {@link android.content.Intent}, and triggers the
     * loading of notifications for the user.</p>
     *
     * @param savedInstanceState The previously saved instance state, or {@code null}
     *                           if this is the first creation.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_received);

        notificationsRepository = NotificationsRepository.getInstance();
        userId = getIntent().getStringExtra("userId");

        if (userId == null) {
            Toast.makeText(this, "Error: No user ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Loading notifications for user: " + userId);

        notificationsContainer = findViewById(R.id.notifications_container);
        emptyStateText = findViewById(R.id.text_empty_state);

        // Back button functionality
        View backButton = findViewById(R.id.buttonBack);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        loadReceivedNotifications();
    }

    /**
     * Loads notifications for the current user through the {@link NotificationsRepository}.
     *
     * <p>This method queries Firestore for all notifications where
     * the {@code receiversId} array contains the user's ID. Upon completion,
     * the UI is updated to either show the list of notifications or an empty/error state.</p>
     */
    private void loadReceivedNotifications() {
        Log.d(TAG, "Loading received notifications via repository for user: " + userId);

        notificationsRepository.fetchNotificationsReceived(
                userId,
                new NotificationsRepository.NotificationsCallback() {

                    /**
                     * Called when notifications are successfully fetched.
                     *
                     * @param notifications A list of notifications received by the user.
                     */
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

                    /**
                     * Called when the notification fetch operation fails.
                     *
                     * @param e The exception describing the failure.
                     */
                    @Override
                    public void onError(@NonNull Exception e) {
                        Log.e(TAG, "Error loading notifications", e);
                        Toast.makeText(
                                NotificationsReceivedActivity.this,
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
     * Creates a styled UI element representing a single notification and adds it to the view.
     *
     * <p>The created view is clickable and opens an
     * {@link com.example.lotterize.ui.admin.adminNotifications.AdminNotificationDetailsDialog}
     * showing the full details of the notification.</p>
     *
     * @param message      The notification message text displayed to the user.
     * @param notification The complete {@link Notification} object containing detailed information.
     */
    private void addNotificationToView(String message, Notification notification) {

        // Container layout for a single notification entry
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

        // Message text view
        TextView messageView = new TextView(this);
        messageView.setText(message != null ? message : "No message");
        messageView.setTextSize(16);
        messageView.setTextColor(0xFF000000);
        messageView.setLineSpacing(4, 1.0f);
        notificationItem.addView(messageView);

        // Clicking opens detailed dialog
        notificationItem.setOnClickListener(v -> {
            AdminNotificationDetailsDialog dialog =
                    AdminNotificationDetailsDialog.newInstance(notification);
            dialog.show(getSupportFragmentManager(), "notification_details");
        });

        // Divider between notifications
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

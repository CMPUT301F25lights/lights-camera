package com.example.lotterize.ui.admin.adminNotifications;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.lotterize.Notification;
import com.example.lotterize.R;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * DialogFragment that shows all details for a single notification in the admin log.
 *
 * Use {@link #newInstance(Notification)} to create the dialog with the data for
 * a particular notification.
 */
public class AdminNotificationDetailsDialog extends DialogFragment {

    private static final String ARG_NOTIFICATION = "arg_notification";
    private final SimpleDateFormat tsFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    /**
     * This is the empty public constructor required for fragments.
     */
    public AdminNotificationDetailsDialog() {}


    /**
     * Creates a new instance of this dialog for the given notification.
     * The notification fields are copied into the dialog's arguments bundle
     * using only primitive types and collections that are safe to pass.
     *
     * @param notification
     *      notification whose details should be displayed
     * @return
     *      a configured dialog fragment
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

        // receiversId is already an ArrayList<String>, so this works directly
        args.putStringArrayList("receivers", new ArrayList<>(notification.getReceiversId()));

        f.setArguments(args);
        return f;
    }

    /**
     * Creates the dialog, inflates the layout, and populates all views with
     * the notification details that were passed in via arguments.
     *
     * @param savedInstanceState
     *      previous saved state for this dialog, or null if there is none
     * @return
     *      the constructed Dialog instance to be shown by the fragment
     */
    @SuppressLint("SetTextI18n")
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_admin_notification_details, null);

        TextView notificationIdTextView = view.findViewById(R.id.text_notification_id);
        TextView senderTextView = view.findViewById(R.id.text_sender);
        TextView timeTextView = view.findViewById(R.id.text_time);
        TextView msgTextView = view.findViewById(R.id.text_message);
        TextView receiversCountTextView = view.findViewById(R.id.text_receivers_count);
        TextView receiversTextView = view.findViewById(R.id.text_receivers);

        Bundle args = getArguments();

        String notificationId = args != null ? args.getString("notificationId") : null;
        String senderName = args != null ? args.getString("senderName") : null;
        String senderId   = args != null ? args.getString("senderId") : null;
        String message    = args != null ? args.getString("message") : null;
        long time   = args != null ? args.getLong("time", -1L) : -1L;
        ArrayList<String> receivers = args != null ? args.getStringArrayList("receivers") : null;

        if (notificationId == null || notificationId.isEmpty()) {
            notificationId = "(No id)";
        }
        notificationIdTextView.setText(notificationId);

        if (senderName == null || senderName.isEmpty()) {
            senderName = "Unknown sender";
        }
        if (senderId == null) {
            senderId = "";
        }
        senderTextView.setText(senderName + " (Id: " + senderId + ")");

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

        int size = (receivers != null) ? receivers.size() : 0;
        receiversCountTextView.setText(String.valueOf(size));

        if (receivers != null && !receivers.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String id : receivers) {
                sb.append("- ").append(id).append("\n");
            }
            receiversTextView.setText(sb.toString());
        } else {
            receiversTextView.setText("No receivers");
        }

        return new AlertDialog.Builder(requireContext())
                .setView(view)
                .setTitle("Notification details")
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                .create();

    }
}

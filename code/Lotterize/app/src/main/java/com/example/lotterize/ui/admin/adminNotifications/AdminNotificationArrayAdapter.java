package com.example.lotterize.ui.admin.adminNotifications;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.lotterize.Notification;
import com.example.lotterize.R;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * ArrayAdapter used by the admin notifications screen to display a list of
 * {@link Notification} objects in a ListView.
 *
 * Each row is inflated from item_admin_notification.xml and shows:
 * - the notification message
 * - the sender name
 * - the time the notification was sent
 * - the number of recipients
 */
public class AdminNotificationArrayAdapter extends ArrayAdapter<Notification> {
    private ArrayList<Notification> notifications;
    private Context context;
    private final SimpleDateFormat tsFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    /**
     * This is the constructor when creating a new adapter for admin notifications.
     *
     * @param context
     *      The context used to inflate views
     * @param notifications
     *      The initial list of notifications to display
     */
    public AdminNotificationArrayAdapter(Context context, ArrayList<Notification> notifications) {
        super(context, 0, notifications);
        this.notifications = notifications;
        this.context = context;
    }

    /**
     * This creates or reuses a row view and binds a {@link Notification} to it.
     *
     * @param position
     *      The position of the item within the adapter's data set
     * @param convertView
     *      The old view to reuse, if possible
     * @param parent
     *      The parent view that this view will eventually be attached to
     * @return
     *      Returns the populated row view for the given position
     */
    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_admin_notification, parent, false);
        }

        Notification notification = notifications.get(position);

        TextView messageTextView = view.findViewById(R.id.text_message);
        TextView senderTextView = view.findViewById(R.id.text_sender);
        TextView timeTextView = view.findViewById(R.id.text_time);
        TextView numberRecipientsTextView = view.findViewById(R.id.text_recipients_summary);

        senderTextView.setText("Sender: " + notification.getSenderName());
        messageTextView.setText(notification.getMessage());
        Timestamp time = notification.getTime();

        ArrayList<String> receivers = notification.getReceiversId();
        int size = (receivers != null) ? receivers.size() : 0;
        numberRecipientsTextView.setText("Recipients: " + String.valueOf(size));

        if (time != null) {
            String formatted = tsFormat.format(time.toDate());
            timeTextView.setText("Time: " + formatted);
        } else {
            timeTextView.setText("");
        }

        return view;
    }


    /**
     * This replaces the current data set with a new list of notifications
     * and refreshes the ListView.
     *
     * @param newList
     *      The new list of notifications to display
     */
    public void updateData(ArrayList<Notification> newList) {
        this.notifications.clear();
        this.notifications.addAll(newList);
        notifyDataSetChanged();
    }
}

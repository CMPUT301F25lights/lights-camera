package com.example.lotterize.ui.notifications;

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
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * This is an adapter class that displays {@link Notification} items
 * in a ListView for the notifications screen.
 */
public class NotificationArrayAdapter extends ArrayAdapter<Notification> {

    private ArrayList<Notification> notifications;
    private Context context;
    private final SimpleDateFormat tsFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    /**
     * This is the constructor when creating a new adapter for notifications.
     *
     * @param context
     *      The context used to inflate views
     * @param notifications
     *      The initial list of notifications to display
     */
    public NotificationArrayAdapter(Context context, ArrayList<Notification> notifications) {
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
            view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        }

        Notification notification = notifications.get(position);

        TextView senderTextView = view.findViewById(R.id.text_sender);
        TextView messageTextView = view.findViewById(R.id.text_message);
        TextView timeTextView = view.findViewById(R.id.text_timestamp);

        senderTextView.setText(notification.getSenderName());
        messageTextView.setText(notification.getMessage());
        Timestamp time = notification.getTime();
        if (time != null) {
            String formatted = tsFormat.format(time.toDate());
            timeTextView.setText(formatted);
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


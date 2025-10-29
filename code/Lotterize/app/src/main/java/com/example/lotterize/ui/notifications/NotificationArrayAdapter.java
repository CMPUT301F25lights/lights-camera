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
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Adapter to display notifications in the ListView in FragmentNotifications.
 */
public class NotificationArrayAdapter extends ArrayAdapter<Notification> {

    private ArrayList<Notification> notifications;
    private Context context;
    private final SimpleDateFormat tsFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public NotificationArrayAdapter(Context context, ArrayList<Notification> notifications) {
        super(context, 0, notifications);
        this.notifications = notifications;
        this.context = context;
    }

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

        senderTextView.setText("User " + notification.getSenderId());
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
     * Replace the list when LiveData updates.
     * Call this from Fragment when ViewModel gives new data.
     */
    public void updateData(ArrayList<Notification> newList) {
        this.notifications.clear();
        this.notifications.addAll(newList);
        notifyDataSetChanged();
    }


}


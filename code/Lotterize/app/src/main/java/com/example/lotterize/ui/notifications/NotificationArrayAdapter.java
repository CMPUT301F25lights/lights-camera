package com.example.lotterize.ui.notifications;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.lotterize.CurrentUser;
import com.example.lotterize.Event;
import com.example.lotterize.LotteryController;
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

        //Buttons to accept and decline
        Button acceptButton = view.findViewById(R.id.button_accept);
        Button declineButton = view.findViewById(R.id.button_decline);


        senderTextView.setText(notification.getSenderName());
        messageTextView.setText(notification.getMessage());
        Timestamp time = notification.getTime();
        if (time != null) {
            String formatted = tsFormat.format(time.toDate());
            timeTextView.setText(formatted);
        } else {
            timeTextView.setText("");
        }


        // Now handle accept/decline clicks ( Note will probably have to refactor this into EventsReg.)
        LotteryController controller = new LotteryController();

        acceptButton.setOnClickListener(v -> {
            String eventId = notification.getRelatedEventId(); // the ID from the Notification object
            String userId = CurrentUser.get().getUserId();     // current user ID

            if (eventId == null) {
                Toast.makeText(context, "No event linked to this notification.", Toast.LENGTH_SHORT).show();
                return;
            }
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("events").document(eventId).get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            Event event = snapshot.toObject(Event.class); // convert Firestore doc to Event object

                            controller.acceptInvitation(event, userId);
                            Toast.makeText(context, "You accepted the invitation!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Event not found.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Error loading event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        declineButton.setOnClickListener(v -> {
            String eventId = notification.getRelatedEventId();
            String userId = CurrentUser.get().getUserId();


            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("events").document(eventId).get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            Event event = snapshot.toObject(Event.class);
                            assert event != null;
                            controller.declineInvitation(event, userId);
                            Toast.makeText(context, "You declined the invitation.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

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


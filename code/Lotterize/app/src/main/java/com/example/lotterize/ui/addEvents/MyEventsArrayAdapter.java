package com.example.lotterize.ui.addEvents;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.lotterize.Event;
import com.example.lotterize.R;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;


/**
 * This is an adapter that displays {@link Event} items in a ListView for the
 * "My Events" screen. Each row shows the event title and date.
 */
public class MyEventsArrayAdapter extends ArrayAdapter<Event> {
    /** Backing list of events rendered by this adapter. */
    private ArrayList<Event> events;
    private Context context;

    /** Date formatter for the event date label. */
    private final SimpleDateFormat tsFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    /**
     * This is the constructor when creating a new adapter for events.
     *
     * @param context
     *      The context used to inflate views
     * @param events
     *      The initial list of events to display
     */
    public MyEventsArrayAdapter(Context context, ArrayList<Event> events) {
        super(context, 0, events);
        this.events = events;
        this.context = context;
    }

    /**
     * This creates or reuses a row view and binds an {@link Event} to it.
     * It sets the event title and a formatted date (if available).
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
            view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        }

        Event event = events.get(position);

        TextView eventTitleTextView = view.findViewById(R.id.text_title);
        TextView eventDateTextView = view.findViewById(R.id.text_date);

        eventTitleTextView.setText(event.getEventName());

        Timestamp time = event.getDate();
        if (time != null) {
            String formatted = tsFormat.format(time.toDate());
            eventDateTextView.setText(formatted);
        } else {
            eventDateTextView.setText("");
        }


        return view;
    }


    /**
     * This replaces the current data set with a new list of events
     * and refreshes the ListView.
     *
     * @param newList
     *      The new list of events to display
     */
    public void updateData(ArrayList<Event> newList) {
        this.events.clear();
        this.events.addAll(newList);
        notifyDataSetChanged();
    }

}

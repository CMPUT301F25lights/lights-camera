package com.example.lotterize.ui.eventsRegistered;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
 * {@code EventsRegisteredArrayAdapter} is a custom {@link ArrayAdapter} implementation
 * used to display a list of {@link Event} objects in a ListView or similar component.
 * <p>
 * Each item displays the event’s title and date, with a chevron icon for potential navigation.
 * This adapter is typically used in the “Registered Events” section of the Lotterize app,
 * allowing users to view all events they are registered for.
 */
public class EventsRegisteredArrayAdapter extends ArrayAdapter<Event> {

    private final ArrayList<Event> eventList;
    private final Context context;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    /**
     * Constructs a new {@code EventsRegisteredArrayAdapter}.
     *
     * @param context   the context of the calling component (usually an Activity or Fragment)
     * @param eventList the list of {@link Event} objects to display
     */
    public EventsRegisteredArrayAdapter(Context context, ArrayList<Event> eventList) {
        super(context, 0, eventList);
        this.eventList = eventList;
        this.context = context;
    }

    /**
     * Provides a view for each item in the list.
     * This method inflates the layout defined in {@code R.layout.item_event},
     * binds event data such as the event name and date to the corresponding TextViews,
     * and sets up an optional click listener for future navigation features.
     *
     * @param position    the position of the item within the adapter’s data set
     * @param convertView the recycled view to reuse, if available
     * @param parent      the parent view that this view will eventually be attached to
     * @return the fully populated view for display
     */
    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position,
                        @Nullable View convertView,
                        @NonNull ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context)
                    .inflate(R.layout.item_event, parent, false);
        }

        Event event = eventList.get(position);

        TextView title = view.findViewById(R.id.text_title);
        TextView date = view.findViewById(R.id.text_date);
        ImageView chevron = view.findViewById(R.id.icon_chevron);

        title.setText(event.getEventName());

        Timestamp ts = event.getDate();
        if (ts != null) {
            date.setText(dateFormat.format(ts.toDate()));
        } else {
            date.setText("Date TBD");
        }

        // Optional (click on each item)
        view.setOnClickListener(v -> {
            // TODO: Navigate to event details when you create that screen
        });

        return view;
    }

    public void updateData(ArrayList<Event> newList) {
        this.eventList.clear();
        this.eventList.addAll(newList);
        notifyDataSetChanged();
    }
}

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

public class EventsRegisteredArrayAdapter extends ArrayAdapter<Event> {

    private final ArrayList<Event> eventList;
    private final Context context;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public EventsRegisteredArrayAdapter(Context context, ArrayList<Event> eventList) {
        super(context, 0, eventList);
        this.eventList = eventList;
        this.context = context;
    }

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

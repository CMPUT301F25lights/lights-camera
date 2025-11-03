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
 * ViewModel for the My Event screen.
 * It owns Firestore listener and transform FireStore documents into Java Object
 * It also exposes LiveData to the Fragment.
 */
public class MyEventsArrayAdapter extends ArrayAdapter<Event> {
    private ArrayList<Event> events;
    private Context context;
    private final SimpleDateFormat tsFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public MyEventsArrayAdapter(Context context, ArrayList<Event> events) {
        super(context, 0, events);
        this.events = events;
        this.context = context;
    }

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
     * Replace the list when LiveData updates.
     * Call this from Fragment when ViewModel gives new data.
     */
    public void updateData(ArrayList<Event> newList) {
        this.events.clear();
        this.events.addAll(newList);
        notifyDataSetChanged();
    }

}

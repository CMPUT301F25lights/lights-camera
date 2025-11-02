package com.example.lotterize.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.lotterize.R;
import com.example.lotterize.UserActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

import kotlinx.serialization.descriptors.PrimitiveKind;

public class EventListArrayAdapter extends ArrayAdapter<DocumentSnapshot> {

    public EventListArrayAdapter(Context context, ArrayList<DocumentSnapshot> events){
        super(context, 0, events);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup
            parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.content_compact_event,
                    parent, false);
        } else {
            view = convertView;
        }

        DocumentSnapshot event = getItem(position);
        TextView eventName = view.findViewById(R.id.event_name);
        TextView eventDescription = view.findViewById(R.id.event_short_description);
        eventName.setText(event.getString("eventName"));

        String desc = event.getString("description");
        if (desc.length() > 50){
            desc = desc.substring(0,49);
            desc += "...";
        }
        eventDescription.setText(desc);

        Button eventDetails = view.findViewById(R.id.event_details_button);
        eventDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),EventDetailsActivity.class);;
                if (getItem(position).getString("eventId") != null){
                    intent.putExtra("eventId", getItem(position).getString("eventId"));
                } else {
                    intent.putExtra("eventId", (String) null);
                }
                getContext().startActivity(intent);
            }
        });
        return view;
    }
}

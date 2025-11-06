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

/**
 * Array adapter that formats an event to be displayed in the
 * home screen
 */
public class EventListArrayAdapter extends ArrayAdapter<DocumentSnapshot> {

    public EventListArrayAdapter(Context context, ArrayList<DocumentSnapshot> events){
        super(context, 0, events);
    }

    /**
     * Formats an event to be displayed in the home screen
     *
     * @param position The position of the item within the adapter's data set of the item whose view
     *        we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *        is non-null and of an appropriate type before using. If it is not possible to convert
     *        this view to display the correct data, this method can create a new view.
     *        Heterogeneous lists can specify their number of view types, so that this View is
     *        always of the right type (see {@link #getViewTypeCount()} and
     *        {@link #getItemViewType(int)}).
     * @param parent The parent that this view will eventually be attached to
     * @return View
     */
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

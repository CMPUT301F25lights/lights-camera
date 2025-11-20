package com.example.lotterize.ui.addEvents.ChosenEntrantsList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.lotterize.Notification;
import com.example.lotterize.R;
import com.example.lotterize.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ChosenEntrantsArrayAdapter extends ArrayAdapter {
    private ArrayList<User> entrants;

    private ArrayList<String> finalEntrantsList;

    private Context context;

    private final OnEntrantActionListener listener;

    /**
     * Listener interface for actions on entrants.
     */
    public interface OnEntrantActionListener {
        void onRemoveClicked(@NonNull User user);
    }

    /**
     * This is the constructor when creating a new adapter for ChosenEntrantsListFragment.
     *
     * @param context
     *      The context used to inflate views
     * @param entrants
     *      The initial list of users to display
     */
    public ChosenEntrantsArrayAdapter(Context context, ArrayList<User> entrants, ArrayList<String> finalEntrantsList, OnEntrantActionListener listener) {
        super(context, 0, entrants);
        this.entrants = entrants;
        this.context = context;
        this.finalEntrantsList = finalEntrantsList;
        this.listener = listener;
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
            view = LayoutInflater.from(context).inflate(R.layout.item_chosen_entrants, parent, false);
        }

        User user = entrants.get(position);

        TextView entrantNameTextView = view.findViewById(R.id.text_entrant_name);
        TextView entrantStatusTextView = view.findViewById(R.id.text_entrant_status);

        if (finalEntrantsList.contains(user.getUserId())){
            entrantNameTextView.setText(user.getName() != null ? user.getName() : "ID: " + user.getUserId());
            entrantStatusTextView.setText("Enrolled");
            entrantStatusTextView.setBackgroundTintList(ContextCompat.getColorStateList(context, (R.color.teal_700)));

            //Make sure organizer cannot modify the enrolled entrants
            entrantStatusTextView.setClickable(false);
            entrantStatusTextView.setFocusable(false);
            entrantStatusTextView.setOnClickListener(null);
        }
        else{
            entrantNameTextView.setText(user.getName() != null ? user.getName() : "ID: " + user.getUserId());
            entrantStatusTextView.setText("Remove");
            entrantStatusTextView.setBackgroundTintList(ContextCompat.getColorStateList(context, (R.color.dark_red_for_blue_bg)));

            entrantStatusTextView.setClickable(true);
            entrantStatusTextView.setFocusable(true);
            entrantStatusTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        listener.onRemoveClicked(user);
                    }
                }
            });
        }


        return view;
    }


    /**
     * This replaces the current data set with a new list of entrant
     * and refreshes the ListView.
     *
     * @param newList
     *      The new list of entrants to display
     */
    public void updateData(ArrayList<User> newList, Collection<String> newFinalList) {
        this.entrants.clear();
        this.entrants.addAll(newList);
        this.finalEntrantsList.clear();
        this.finalEntrantsList.addAll(newFinalList);
        notifyDataSetChanged();
    }
}

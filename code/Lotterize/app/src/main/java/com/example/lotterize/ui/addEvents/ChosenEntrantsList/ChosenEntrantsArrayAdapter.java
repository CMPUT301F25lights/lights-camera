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

/**
 * {@code ChosenEntrantsArrayAdapter} is a custom {@link ArrayAdapter} that displays
 * the list of chosen entrants for an event in {@link ChosenEntrantsListFragment}.
 * <p>
 * Each row shows:
 * <ul>
 *     <li>The entrant's display name (or their ID if the name is unavailable).</li>
 *     <li>A status that is either:
 *         <ul>
 *             <li><b>"Enrolled"</b> (non-clickable) for entrants already enrolled, or</li>
 *             <li><b>"Remove"</b> (clickable) for entrants that can be removed, which
 *                 will trigger a callback to {@link OnEntrantActionListener}.</li>
 *         </ul>
 *     </li>
 * </ul>
 */
public class ChosenEntrantsArrayAdapter extends ArrayAdapter {
    private ArrayList<User> entrants;

    /**
     * The list of user IDs that are considered "final" entrants.
     * Any entrant whose user ID is contained in this list is shown as enrolled
     * and cannot be modified from the UI.
     */
    private ArrayList<String> finalEntrantsList;

    private Context context;


    /**
     * Listener used to propagate row-level actions (e.g., "Remove") back to the
     * hosting fragment or activity.
     */
    private final OnEntrantActionListener listener;

    /**
     * Listener interface for actions on entrants.
     * <p>
     * The hosting fragment/activity should implement this interface
     * and pass an instance to the adapter so that it can respond to
     * user interactions on individual rows.
     */
    public interface OnEntrantActionListener {

        /**
         * Called when the "Remove" action is clicked for a given entrant.
         *
         * @param user The {@link User} that the organizer wants to remove.
         */
        void onRemoveClicked(@NonNull User user);
    }

    /**
     * This constructs a new {@code ChosenEntrantsArrayAdapter} for use in
     * {@link ChosenEntrantsListFragment}.
     *
     * @param context           the context used to inflate row views and access resources
     * @param entrants          the initial list of {@link User} objects to display
     * @param finalEntrantsList the list of user IDs that are already enrolled and
     *                          therefore should be shown as non-removable
     * @param listener          callback for handling actions on entrants (e.g., removal)
     */
    public ChosenEntrantsArrayAdapter(Context context, ArrayList<User> entrants, ArrayList<String> finalEntrantsList, OnEntrantActionListener listener) {
        super(context, 0, entrants);
        this.entrants = entrants;
        this.context = context;
        this.finalEntrantsList = finalEntrantsList;
        this.listener = listener;
    }

    /**
     * This creates or reuses a row view and binds a {@link User} to it.
     *
     * @param position    the position of the item within the adapter's data set
     * @param convertView the old view to reuse, if possible; may be {@code null}
     * @param parent      the parent that this view will eventually be attached to
     * @return the populated row view for the given position
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
            // Show removable entrants with a "Remove" action
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
     * This replaces the current data set with a new list of entrants and an updated list
     * of final (enrolled) entrant IDs, then refreshes the attached {@link android.widget.ListView}.
     *
     * @param newList      the new list of {@link User} objects to display
     * @param newFinalList the new collection of user IDs that should be treated as final/enrolled
     */
    public void updateData(ArrayList<User> newList, Collection<String> newFinalList) {
        this.entrants.clear();
        this.entrants.addAll(newList);
        this.finalEntrantsList.clear();
        this.finalEntrantsList.addAll(newFinalList);
        notifyDataSetChanged();
    }
}

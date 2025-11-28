package com.example.lotterize.ui.addEvents.ChosenEntrantsList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotterize.CurrentUser;
import com.example.lotterize.Notification;
import com.example.lotterize.NotificationSender;
import com.example.lotterize.R;
import com.example.lotterize.User;
import com.example.lotterize.databinding.FragmentAllEntrantsBinding;
import com.example.lotterize.databinding.FragmentChosenEntrantsBinding;
import com.example.lotterize.ui.addEvents.EntrantListFragment;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * {@code ChosenEntrantsListFragment} displays the list of chosen entrants for a given event.
 * <p>
 * It listens to changes on the corresponding {@code events/{eventId}} document and:
 * <ul>
 *     <li>Shows all entrants in the {@code selectedList} field of the event.</li>
 *     <li>Marks entrants in {@code finalList} as enrolled and non-removable.</li>
 *     <li>Allows the organizer to move a chosen entrant to {@code cancelledList} via
 *         a "Remove" action.</li>
 * </ul>
 * The fragment uses {@link ChosenEntrantsArrayAdapter} to render each entrant row.
 */
public class ChosenEntrantsListFragment extends Fragment {
    private String eventId;

    private ChosenEntrantsArrayAdapter adapter;

    /**
     * List of {@link User} objects representing chosen entrants (selectedList).
     */
    private final ArrayList<User> chosenEntrants = new ArrayList<>();

    /**
     * List of user IDs representing final/enrolled entrants (finalList).
     * These IDs are passed into the adapter so enrolled entrants can be marked non-removable.
     */
    private final ArrayList<String> finalEntrantsList = new ArrayList<>();
    FragmentChosenEntrantsBinding binding;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Registration handle for the Firestore snapshot listener on the event document.
     * It is removed in {@link #onDestroyView()} to avoid leaks.
     */
    private ListenerRegistration registration;

    /**
     * This inflates the fragment layout and initializes the view binding.
     *
     * @param inflater
     *      The LayoutInflater used to inflate views in this fragment
     * @param container
     *      The parent view that the fragment's UI will attach to (if non-null)
     * @param savedInstanceState
     *      If non-null, this fragment is being re-created from a previous state
     * @return
     *      Returns the root view of the fragment layout
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChosenEntrantsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Configures the UI and starts loading data:
     * <ul>
     *     <li>Sets up the back button to pop the back stack.</li>
     *     <li>Reads the {@code eventId} from fragment arguments.</li>
     *     <li>Initializes the {@link ChosenEntrantsArrayAdapter} and attaches it to the ListView.</li>
     *     <li>Begins listening to the event document for chosen / final entrant updates.</li>
     * </ul>
     *
     * @param v                  the root view returned by {@link #onCreateView}
     * @param savedInstanceState previous state if the fragment is being re-created
     */
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        v.findViewById(R.id.button_back).setOnClickListener(
                view -> NavHostFragment.findNavController(ChosenEntrantsListFragment.this).popBackStack()
        );

        Bundle args = getArguments();
        eventId = (args != null) ? args.getString("eventId") : null;

        if (eventId == null) {
            Toast.makeText(requireContext(),"Missing eventId/status", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).popBackStack();
            return;
        }

        ListView listView = binding.listChosenEntrants;
        adapter = new ChosenEntrantsArrayAdapter(requireContext(),
                chosenEntrants,
                finalEntrantsList,
                entrant -> cancelChosenEntrant(entrant.getUserId())
        );

        listView.setAdapter(adapter);

        startListeningToEvent();
    }

    /**
     * Starts a Firestore snapshot listener on the current event document.
     * <p>
     * Whenever the event document changes:
     * <ul>
     *     <li>Reads {@code selectedList} to determine which users are chosen.</li>
     *     <li>Reads {@code finalList} to determine which chosen entrants are enrolled.</li>
     *     <li>Fetches details for all chosen entrants from the {@code users} collection.</li>
     *     <li>Updates the adapter so the UI reflects the latest state.</li>
     * </ul>
     * Any existing in-memory lists are cleared and repopulated based on the snapshot.
     */
    private void startListeningToEvent() {
        registration = db.collection("events")
                .document(eventId)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        Toast.makeText(requireContext(),"Failed to load entrants: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snap == null || !snap.exists()) {
                        Toast.makeText(requireContext(),"Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ArrayList<String> chosenList = (ArrayList<String>) (snap.get("selectedList") != null ? snap.get("selectedList") : new ArrayList<String>());
                    ArrayList<String> finalList = (ArrayList<String>) (snap.get("finalList") != null ? snap.get("finalList") : new ArrayList<String>());

                    chosenEntrants.clear();
                    if (chosenList != null) {
                        fetchChosenUsers(chosenList);
                    }

                    finalEntrantsList.clear();
                    if (finalList != null) {
                        finalEntrantsList.addAll(finalList);
                    }
                    adapter.notifyDataSetChanged();

                });
    }

    /**
     * This fetches {@link User} documents for the given list of user IDs from the {@code users} collection.
     * <p>
     * Because {@code whereIn} is limited to 10 values per query, the list of IDs is split into
     * chunks of size 10 or less, and multiple queries are performed. Once all queries succeed:
     * <ul>
     *     <li>{@link #chosenEntrants} is repopulated with the fetched user data.</li>
     *     <li>The adapter is notified to refresh the UI.</li>
     *     <li>If no users are found, a toast is shown.</li>
     * </ul>
     *
     * @param chosenIds list of user IDs in the event's {@code selectedList}
     */
    private void fetchChosenUsers(ArrayList<String> chosenIds) {

        ArrayList<Task<QuerySnapshot>> tasks = new ArrayList<>();

        for (int i = 0; i < chosenIds.size(); i += 10) {
            ArrayList<String> chunk = new ArrayList<>(chosenIds.subList(i, Math.min(i + 10, chosenIds.size())));

            Task<QuerySnapshot> t = db.collection("users")
                    .whereIn(FieldPath.documentId(), chunk)
                    .get();

            tasks.add(t);
        }

        Tasks.whenAllSuccess(tasks)
                .addOnSuccessListener(results -> {
                    chosenEntrants.clear();

                    // results is a List<Object> where each Object is a QuerySnapshot
                    for (Object o : results) {
                        QuerySnapshot qs = (QuerySnapshot) o;
                        for (DocumentSnapshot doc : qs) {
                            if (!doc.exists()) continue;

                            User user = new User();
                            user.setName(doc.getString("name"));
                            user.setUserId(doc.getId());

                            chosenEntrants.add(user);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    if(chosenEntrants.isEmpty()){
                        Toast.makeText(requireContext(), "No entrants in the list",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(err -> {
                    Toast.makeText(requireContext(), "Failed to load user info: " + err.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    /**
     * Moves a chosen entrant from the event's {@code selectedList} to {@code cancelledList}
     * after the organizer confirms through a dialog.
     *
     * @param userId the ID of the entrant to cancel
     */
    private void cancelChosenEntrant(@NonNull String userId) {
        // Show confirmation dialog first
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancel entrant")
                .setMessage("Are you sure you want to cancel this entrant?")
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton("Yes", (dialog, which) -> {
                    db.collection("events").document(eventId).get()
                            .addOnSuccessListener(doc -> {
                                if (!doc.exists()){
                                    Toast.makeText(requireContext(),
                                            "Event not found", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                String eventName = doc.getString("eventName");
                                db.collection("events").document(eventId)
                                        .update(
                                                "selectedList", FieldValue.arrayRemove(userId),
                                                "cancelledList", FieldValue.arrayUnion(userId)
                                        )
                                        .addOnSuccessListener(unused -> {
                                            Toast.makeText(requireContext(),
                                                    "Entrant cancelled", Toast.LENGTH_SHORT).show();

                                            String message = "You have been cancelled from the " + (eventName != null ? eventName : "") + "event";

                                            NotificationSender sender = new NotificationSender();
                                            sender.sendNotification(CurrentUser.get().getUserId(), message, new ArrayList<>(Collections.singletonList(userId))
                                            );
                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(requireContext(), "Failed to cancel entrant: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                        );
                            });
                })
                .show();
    }

    /**
     * This cleans up resources when the view is destroyed:
     * <ul>
     *     <li>Removes the Firestore snapshot listener (if any) to avoid memory leaks.</li>
     *     <li>Clears the view binding reference.</li>
     * </ul>
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (registration != null) {
            registration.remove();
            registration = null;
        }
        binding = null;
    }

}

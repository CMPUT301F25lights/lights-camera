package com.example.lotterize.ui.addEvents;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotterize.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a reusable fragment that displays entrants for a single list of an {@code Event}
 * (e.g., WAITLIST, CHOSEN, CANCELLED, ENROLLED). It shows each entrant's display name if
 * found in the {@code users} collection, otherwise falls back to their id.
 */
public class EntrantListFragment extends Fragment {

    private String eventId;
    private String status;

    private TextView title;
    private ListView listView;

    private ArrayAdapter<String> adapter;
    private final ArrayList<String> rows = new ArrayList<>();

    /** Cache mapping user document id -> display name (fallback to id). */
    private final Map<String, String> idToNameMap = new HashMap<>();

    /**
     * This inflates the fragment layout.
     *
     * @param inflater
     *      The LayoutInflater used to inflate views
     * @param container
     *      The parent view to attach to (if non-null)
     * @param savedInstanceState
     *      Previous state if the fragment is being re-created
     * @return
     *      Returns the root view of the fragment layout
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_list, container, false);
    }

    /**
     * This configures the UI and triggers data loading:
     *   - Reads {@code eventId} and {@code status} from arguments
     *   - Sets the toolbar back button
     *   - Initializes the ListView and its adapter
     *   - Loads entrants for the requested list
     *
     * @param v
     *      The root view returned by {@link #onCreateView}
     * @param savedInstanceState
     *      Previous state if the fragment is being re-created
     */
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        title = v.findViewById(R.id.title);
        listView = v.findViewById(R.id.list);

        v.findViewById(R.id.button_back).setOnClickListener(
                view -> NavHostFragment.findNavController(EntrantListFragment.this).popBackStack()
        );

        Bundle args = getArguments();
        eventId = (args != null) ? args.getString("eventId") : null;
        status  = (args != null) ? args.getString("status")  : null;

        if (eventId == null || status == null) {
            toast("Missing eventId/status");
            NavHostFragment.findNavController(this).popBackStack();
            return;
        }

        title.setText(ListLabel(status));

        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, rows);
        listView.setAdapter(adapter);

        loadEntrants();
    }

    /**
     * This returns a title for the list based on status.
     *
     * @param s
     *      The entrant list status (WAITLIST/CHOSEN/CANCELLED/ENROLLED)
     * @return
     *      Returns the page title to display
     */
    private String ListLabel(String s) {
        switch (s) {
            case "WAITLIST":  return "Waitlist Entrants";
            case "CHOSEN":    return "Chosen Entrants";
            case "CANCELLED": return "Cancelled Entrants";
            case "ENROLLED":  return "Enrolled Entrants";
            default:          return "Entrants";
        }
    }

    /**
     * This maps a status to its Firestore field name in the event document.
     *
     * @param s
     *      The entrant list status (WAITLIST/CHOSEN/CANCELLED/ENROLLED)
     * @return
     *      Returns the field name on the event document, or {@code null} if unknown
     */    @Nullable
    private String listField(String s) {
        switch (s) {
            case "WAITLIST":  return "waitList";
            case "CHOSEN":    return "selectedList";
            case "CANCELLED": return "cancelledList";
            case "ENROLLED":  return "finalList";
            default:          return null;
        }
    }

    /**
     * This loads the entrant ids for the requested list from the event document.
     */
    private void loadEntrants() {
        final String field = listField(status);
        if (field == null) {
            toast("Unknown list type");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        bindFromDoc(doc, field);
                    } else {
                        toast("Failed to look up the list");
                        return;
                    }
                })
                .addOnFailureListener(e -> toast("Lookup failed: " + e.getMessage()));
    }

    /**
     * This extracts entrant ids from the event document and triggers name resolution.
     *
     * @param doc
     *      The event document snapshot
     * @param field
     *      The field name that holds the entrant id list
     */
    @SuppressWarnings("unchecked")
    private void bindFromDoc(@NonNull DocumentSnapshot doc, @NonNull String field) {
        rows.clear();

        // Read the raw value from the event document for the given list field
        Object rawList = doc.get(field);

        // If the field is missing or not an array/list, there are no entrants to show
        if (!(rawList instanceof List)) {
            adapter.notifyDataSetChanged();
            toast("No entrants in this list");
            return;
        }


        ArrayList<String> listOfEntrantsId = (ArrayList<String>) rawList;

        // If the list exists but is empty, inform the user and exit early
        if (listOfEntrantsId.isEmpty()) {
            adapter.notifyDataSetChanged();
            toast("No entrants in this list");
            return;
        }

        // We have a non-empty set of user document ids → resolve each to a display name
        resolveNamesByDocId(listOfEntrantsId);
    }


    /**
     * This preloads all user documents to build an in-memory cache (id -> name),
     * then maps the provided entrant ids to names locally.
     *
     * @param docIds
     *      The entrant user ids to display in order
     */    private void resolveNamesByDocId(@NonNull ArrayList<String> docIds) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .get()
                .addOnSuccessListener(qs -> {
                    for (DocumentSnapshot d : qs.getDocuments()) {
                        String id   = d.getId();
                        String name = d.getString("name");
                        idToNameMap.put(id, (name != null && !name.isEmpty()) ? name : id);
                    }
                    applyNames(docIds);
                })
                .addOnFailureListener(e -> {

                    // If we fail to build the cache, show raw ids
                    rows.clear();
                    rows.addAll(docIds);
                    adapter.notifyDataSetChanged();
                    Log.w("EntrantListFragment", "Failed to preload users: " + e.getMessage());
                });
    }

    /**
     * This fills {@link #rows} using {@link #idToNameMap} in the same order as the given ids,
     * preferring display names and falling back to the raw id if a name is not found.
     *
     * @param docIds
     *      The entrant ids in display order
     */
    private void applyNames(@NonNull ArrayList<String> docIds) {
        rows.clear();
        for (String id : docIds) {
            String name = idToNameMap.get(id);
            rows.add((name != null && !name.isEmpty()) ? name : id);
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * This shows a short toast message.
     *
     * @param msg
     *      The message to display
     */
    private void toast(String msg) { Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show(); }
}

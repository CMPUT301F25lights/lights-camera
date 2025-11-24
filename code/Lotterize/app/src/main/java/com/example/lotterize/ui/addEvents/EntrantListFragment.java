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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code EntrantListFragment} is a reusable fragment that displays entrants for
 * a single list of an {@code Event}
 * <p>
 * It:
 * <ul>
 *     <li>Reads {@code eventId} and {@code status} from arguments.</li>
 *     <li>Listens in real time to the corresponding list field on the event document.</li>
 *     <li>Resolves user IDs to their display names from the {@code users} collection.</li>
 *     <li>Falls back to the raw user ID when a display name is missing.</li>
 * </ul>
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

    ListenerRegistration registration;

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

        startEntrantsListener();
    }

    /**
     * This returns a title for the list based on status.
     *
     * @param s
     *      The entrant list status (WAITLIST/CANCELLED/ENROLLED)
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
     *      The entrant list status (WAITLIST/CANCELLED/ENROLLED)
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
     * This starts a real-time listener on the event document and refreshes the
     * entrant list whenever the document changes in Firestore.
     */
    private void startEntrantsListener() {
        final String field = listField(status);
        if (field == null) {
            toast("Unknown list type");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //Clean up existing listener in case we called it twice
        if (registration != null){
            registration.remove();
            registration = null;
        }
        registration =
                db.collection("events").document(eventId)
                        .addSnapshotListener((doc, e)->{
                            if (e != null) {
                                toast("Listen failed: " + e.getMessage());
                                return;
                            }
                            if(doc != null && doc.exists()){
                                bindFromDoc(doc, field);
                            }
                            else{
                                toast("Failed to load event");
                                rows.clear();
                                adapter.notifyDataSetChanged();
                            }
                        });
    }

    /**
     * This is called when the view hierarchy associated with the fragment is being removed.
     * We remove the Firestore snapshot listener here to avoid memory leaks and
     * unnecessary network usage once the user leaves this screen.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (registration != null) {
            registration.remove();
            registration = null;
        }
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
     */
    private void resolveNamesByDocId(@NonNull ArrayList<String> docIds) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .get()
                .addOnSuccessListener(qs -> {
                    for (DocumentSnapshot d : qs.getDocuments()) {
                        String id   = d.getId();
                        String name = d.getString("name");
                        idToNameMap.put(id, (name != null && !name.isEmpty()) ? name : id);
                    }
                    rows.clear();
                    rows.addAll(applyNames(docIds, idToNameMap));
                    adapter.notifyDataSetChanged();                })
                .addOnFailureListener(e -> {

                    // If we fail to build the cache, show raw ids
                    rows.clear();
                    rows.addAll(docIds);
                    adapter.notifyDataSetChanged();
                    Log.w("EntrantListFragment", "Failed to preload users: " + e.getMessage());
                });
    }

    /**
     * This resolves display names for the given user IDs using the provided lookup map,
     * preserving the order of {@code docIds}. If an ID is absent from the map or
     * its mapped value is {@code null} or empty, the ID itself is used as a fallback.
     *
     *
     * @param docIds       ordered list of user document IDs to resolve (non-null)
     * @param idToNameMap  map of userId → display name; may be empty or missing entries
     * @return a new list of display strings corresponding 1-to-1 with {@code docIds}
     */
    public static ArrayList<String> applyNames(@NonNull ArrayList<String> docIds, Map<String, String> idToNameMap) {
        ArrayList<String> userNames = new ArrayList<String>();
        for (String id : docIds) {
            String name = idToNameMap.get(id);
            userNames.add((name != null && !name.isEmpty()) ? name : id);
        }
        return userNames;
    }

    /**
     * This shows a short toast message.
     *
     * @param msg
     *      The message to display
     */
    private void toast(String msg) { Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show(); }
}

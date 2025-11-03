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
 * Reusable list screen that shows entrants for one Event list (WAITLIST/CHOSEN/CANCELLED/ENROLLED).
 * Shows the user's "name" only.
 */
public class EntrantListFragment extends Fragment {

    private String eventId;
    private String status;

    private TextView title;
    private ListView listView;

    private ArrayAdapter<String> adapter;
    private final ArrayList<String> rows = new ArrayList<>();

    private final Map<String, String> idToNameMap = new HashMap<>();
    private boolean isUserCacheLoaded = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_list, container, false);
    }

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
     * Returns the label of the fragment page
     * **/
    private String ListLabel(String s) {
        switch (s) {
            case "WAITLIST":  return "Waitlist Entrants";
            case "CHOSEN":    return "Chosen Entrants";
            case "CANCELLED": return "Cancelled Entrants";
            case "ENROLLED":  return "Enrolled Entrants";
            default:          return "Entrants";
        }
    }

    /** Firestore field name containing the list. */
    @Nullable
    private String listField(String s) {
        switch (s) {
            case "WAITLIST":  return "waitList";
            case "CHOSEN":    return "selectedList";
            case "CANCELLED": return "cancelledList";
            case "ENROLLED":  return "finalList";
            default:          return null;
        }
    }

    // ---------- Data loading ----------
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
                        db.collection("events")
                                .whereEqualTo("eventId", eventId)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(snap -> {
                                    if (!snap.isEmpty()) {
                                        bindFromDoc(snap.getDocuments().get(0), field);
                                    } else {
                                        toast("Event not found");
                                        NavHostFragment.findNavController(this).popBackStack();
                                    }
                                })
                                .addOnFailureListener(e -> toast("Lookup failed: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> toast("Lookup failed: " + e.getMessage()));
    }

    @SuppressWarnings("unchecked")
    private void bindFromDoc(@NonNull DocumentSnapshot doc, @NonNull String field) {
        rows.clear();

        Object rawList = doc.get(field);

        if (!(rawList instanceof List)) {
            adapter.notifyDataSetChanged();
            toast("No entrants in this list");
            return;
        }

        ArrayList<?> listOfEntrantsId = (ArrayList<?>) rawList;
        if (listOfEntrantsId.isEmpty()) {
            adapter.notifyDataSetChanged();
            toast("No entrants in this list");
            return;
        }

        Object first = listOfEntrantsId.get(0);

        // Otherwise, assume list of primitive ids (strings)
        ArrayList<String> ids = new ArrayList<>();
        for (Object entrantId : listOfEntrantsId){
            if (entrantId != null) {
                ids.add(String.valueOf(entrantId));
            }
        }

        if (ids.isEmpty()) {
            adapter.notifyDataSetChanged();
            toast("No entrants in this list");
            return;
        }
        resolveNamesByDocId(ids);
    }


    /** Resolve display names by loading all /users once (id -> name), then map locally. */
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
                    isUserCacheLoaded = true;
                    applyNames(docIds);
                })
                .addOnFailureListener(e -> {
                    // If we fail to build the cache, at least show raw ids
                    rows.clear();
                    rows.addAll(docIds);
                    adapter.notifyDataSetChanged();
                    Log.w("EntrantListFragment", "Failed to preload users: " + e.getMessage());
                });
    }

    /** Use the in-memory cache to fill rows in the same order as docIds. */
    private void applyNames(@NonNull ArrayList<String> docIds) {
        rows.clear();
        for (String id : docIds) {
            String name = idToNameMap.get(id);
            rows.add((name != null && !name.isEmpty()) ? name : id);
        }
        adapter.notifyDataSetChanged();
    }

    private String asString(Object o) { return (o == null) ? "" : String.valueOf(o); }
    private void toast(String msg) { Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show(); }
}

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ChosenEntrantsListFragment extends Fragment {
    private String eventId;

    private ChosenEntrantsArrayAdapter adapter;
    private final ArrayList<User> chosenEntrants = new ArrayList<>();

    private final ArrayList<String> finalEntrantsList = new ArrayList<>();
    FragmentChosenEntrantsBinding binding;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
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
                })
                .addOnFailureListener(err -> {
                    Toast.makeText(requireContext(), "Failed to load user info: " + err.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void cancelChosenEntrant(@NonNull String userId) {
        // Show confirmation dialog first
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancel entrant")
                .setMessage("Are you sure you want to cancel this entrant?")
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton("Yes", (dialog, which) -> {
                    db.collection("events").document(eventId)
                            .update(
                                    "selectedList", FieldValue.arrayRemove(userId),
                                    "cancelledList", FieldValue.arrayUnion(userId)
                            )
                            .addOnSuccessListener(unused ->
                                    Toast.makeText(requireContext(),"Entrant cancelled", Toast.LENGTH_SHORT).show()
                            )
                            .addOnFailureListener(e ->
                                            Toast.makeText(requireContext(),"Failed to cancel entrant: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .show();
    }

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

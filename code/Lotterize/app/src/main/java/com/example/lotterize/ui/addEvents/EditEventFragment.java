package com.example.lotterize.ui.addEvents;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotterize.Event;
import com.example.lotterize.R;
import com.example.lotterize.databinding.FragmentEditEventBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * This is a fragment that displays and edits details for a single {@link Event}.
 * It loads an event by {@code eventId} argument from Firestore, binds fields to the UI,
 * allows navigation to the entrants screen, and toggles geolocation for the event.
 */
public class EditEventFragment extends Fragment {

    private FragmentEditEventBinding binding;

    private FirebaseFirestore db;

    /** Reference to the current event document for in-place updates (e.g., toggles). */
    private DocumentReference eventDocRef;

    /** Date formatter for the event date label. */
    private final DateFormat dateFmt = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());

    /** Time formatter for the event time label. */
    private final DateFormat timeFmt = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    private ActivityResultLauncher<Intent> saveQrLauncher;

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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // MUST match fragment_edit_event.xml
        binding = FragmentEditEventBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * This wires up interactions and data once the view exists:
     * - Initializes Firestore
     * - Reads the {@code eventId} argument
     * - Sets navigation for the back button and entrants row
     * - Listens to Firestore for the event snapshot and binds it to the UI
     * - Updates the {@code geolocationEnabled} field when the switch toggles
     *
     * @param view
     *      The root view returned by {@link #onCreateView}
     * @param savedInstanceState
     *      If non-null, this fragment is being re-created from a previous state
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        final String eventIdArg = getEventIdArg();

        //Go back to previous page if eventId is missing
        if (eventIdArg == null || eventIdArg.isEmpty()) {
            Toast.makeText(requireContext(), "Missing eventId", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).popBackStack();
            return;
        }

        // Back navigation
        binding.buttonCancelCreateEvent.setOnClickListener(v ->
                NavHostFragment.findNavController(EditEventFragment.this).popBackStack()
        );

        // Entrants row -> navigate with args
        binding.rowEntrants.setOnClickListener(v -> {
            Bundle b = new Bundle();
            b.putString("eventId", eventIdArg);
            NavController nav = NavHostFragment.findNavController(EditEventFragment.this);
            nav.navigate(R.id.navigation_entrantsFragment, b);
        });

        binding.switchGeolocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (eventDocRef != null) eventDocRef.update("geolocationEnabled", isChecked);
        });

        db.collection("events")
                .whereEqualTo("eventId", eventIdArg)
                .limit(1)
                .addSnapshotListener((snap, err) -> {
                    if (err != null) {
                        Toast.makeText(requireContext(), "Failed to load event", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snap == null || snap.isEmpty()) {
                        Toast.makeText(requireContext(), "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    DocumentSnapshot doc = snap.getDocuments().get(0);
                    eventDocRef = doc.getReference();

                    Event e = Event.addEventDetailsFromSnapShot(doc);
                    bindEventToUi(e, doc);
                });
    }

    /**
     * This binds a loaded {@link Event} and its backing document to the UI fields.
     * It formats date/time, fills textual fields, and applies the geolocation toggle state.
     *
     * @param e
     *      The event model built from the Firestore document
     * @param doc
     *      The raw document snapshot (used for fields not in the model)
     */
    private void bindEventToUi(@NonNull Event e, @NonNull DocumentSnapshot doc) {
        binding.tvEventNameValue.setText(e.getEventName());

        Timestamp ts = e.getDate();
        if (ts != null) {
            binding.tvDateValue.setText(dateFmt.format(ts.toDate()));
            binding.tvTimeValue.setText(timeFmt.format(ts.toDate()));
        } else {
            binding.tvDateValue.setText("");
            binding.tvTimeValue.setText("");
        }

        binding.tvLocationValue.setText(e.getLocation());
        binding.tvTotalSpotsValue.setText(String.valueOf(e.getTotalSpots()));
        binding.tvDescriptionValue.setText(e.getDescription());
        binding.tvWaitlistValue.setText(String.valueOf(e.getEntrantsLimit()));
        binding.tvSampleAttendeesValue.setText(String.valueOf(
                e.getFinalList() != null ? e.getFinalList().size() : 0));

        Boolean geo = doc.getBoolean("geolocationEnabled");
        if (geo != null) binding.switchGeolocation.setChecked(geo);
    }

    /**
     * This reads the {@code eventId} from the fragment arguments and normalizes
     * blank values to {@code null}.
     *
     * @return
     *      Returns the event id if present and non-empty; otherwise {@code null}
     */
    private @Nullable String getEventIdArg() {
        Bundle args = getArguments();
        if (args == null) return null;
        String v = args.getString("eventId");
        return (v == null || v.isEmpty()) ? null : v;
    }

    /**
     * This clears the binding reference when the view is destroyed to avoid memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

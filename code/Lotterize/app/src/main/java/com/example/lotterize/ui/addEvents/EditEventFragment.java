package com.example.lotterize.ui.addEvents;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

public class EditEventFragment extends Fragment {

    private FragmentEditEventBinding binding;

    private FirebaseFirestore db;
    private DocumentReference eventDocRef;

    private final DateFormat dateFmt = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
    private final DateFormat timeFmt = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // MUST match fragment_edit_event.xml
        binding = FragmentEditEventBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        final String eventIdArg = getEventIdArg();

        binding.buttonCancelCreateEvent.setOnClickListener(v ->
                NavHostFragment.findNavController(EditEventFragment.this).popBackStack()
        );

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

    private @Nullable String getEventIdArg() {
        Bundle args = getArguments();
        if (args == null) return null;
        String v = args.getString("eventId");
        return (v == null || v.isEmpty()) ? null : v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

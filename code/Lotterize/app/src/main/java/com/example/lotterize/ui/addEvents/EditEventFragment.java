package com.example.lotterize.ui.addEvents;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotterize.Event;
import com.example.lotterize.ImageHandler;
import com.example.lotterize.QR;
import com.example.lotterize.R;
import com.example.lotterize.databinding.FragmentEditEventBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * {@code EditEventFragment} displays and edits details for a single {@link Event}.
 * <p>
 * It:
 * <ul>
 *     <li>Loads an event from Firestore using an {@code eventId} argument.</li>
 *     <li>Binds event fields (name, date, time, location, description, etc.) to the UI.</li>
 *     <li>Allows navigation to the entrants management screen.</li>
 *     <li>Toggles geolocation for the event.</li>
 *     <li>Supports saving the event QR code as a PNG using the system file picker.</li>
 *     <li>Supports selecting, uploading, and removing the event poster image.</li>
 * </ul>
 */
public class EditEventFragment extends Fragment {

    private FragmentEditEventBinding binding;
    private ListenerRegistration eventRegistration;

    private FirebaseFirestore db;

    /** Reference to the current event document for in-place updates (e.g., toggles). */
    private DocumentReference eventDocRef;

    /** Date formatter for the event date label. */
    private final DateFormat dateFmt = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());

    /** Time formatter for the event time label. */
    private final DateFormat timeFmt = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    private String currentQrCode;
    private ActivityResultLauncher<String> saveQrLauncher;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private ImageHandler imageHandler;
    private TextView posterTextView;

    /**
     * Initializes the fragment by setting up:
     * <ul>
     *     <li>QR code save launcher</li>
     *     <li>Firebase Storage references</li>
     *     <li>Photo picker launcher for selecting and uploading event posters</li>
     * </ul>
     *
     * @param savedInstanceState Bundle containing previous state, or null if fresh.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the SAF launcher for saving QR codes
        saveQrLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("image/png"),
                uri -> {
                    if (uri != null) {
                        if (currentQrCode == null || currentQrCode.isEmpty()) {
                            Toast.makeText(requireContext(), "No QR code available", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String status = QR.saveQrCodeToUri(getContext(), uri, currentQrCode);
                        Toast.makeText(requireContext(), status, Toast.LENGTH_SHORT).show();

                    }
                }
        );

        // media picker for poster image
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                binding.posterTextView.setText(uri.toString());
                binding.posterTextView.setVisibility(View.VISIBLE);
                imageHandler.addImage(getContext(), uri,
                        () -> {
                            if (!isAdded() || getContext() == null) return;
                            // Immediately update event document
                            if(eventDocRef!=null){
                                eventDocRef.update("imageUrl", imageHandler.getUploadedImageUrl(),
                                        "imagePath", imageHandler.getUploadedImagePath());
                            }},
                        () -> {
                            // Handle failure if needed
                        }
                );
            }
        });
    }

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
     * - Updates the {@code isGeolocationEnabled} field when the switch toggles
     * - Handles poster image selection, upload, and removal
     * - Sets up QR code save button
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
        imageHandler = new ImageHandler();
        final String eventIdArg = getEventIdArg();
        assert eventIdArg != null;
        eventDocRef = db.collection("events").document(eventIdArg);

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
            if (eventDocRef != null) eventDocRef.update("isGeolocationEnabled", isChecked);
        });

        // save qr code png button
        binding.buttonQRCode.setOnClickListener(v -> {
            if (currentQrCode != null && !currentQrCode.isEmpty()) {
                // Launch file picker with suggested filename
                String filename = "event_qr_code.png";
                saveQrLauncher.launch(filename);
            } else {
                Toast.makeText(requireContext(), "QR code not available", Toast.LENGTH_SHORT).show();
            }
        });

        posterTextView = binding.posterTextView;

        // change/add poster button
        binding.buttonChangePoster.setOnClickListener(v -> pickMedia.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()
        ));

        // remove poster button
        binding.buttonRemovePoster.setOnClickListener(v -> {
            imageHandler.removeImage(getContext(), ()->{
                if (!isAdded() || getContext() == null) return;
                if(eventDocRef != null) eventDocRef.update("imageUrl", null, "imagePath", null);
            });
            posterTextView.setVisibility(View.GONE);
        });

        // Listen for event document changes and bind to UI
        eventRegistration = db.collection("events")
                .document(eventIdArg)
                .addSnapshotListener((doc, err) -> {
                    if (err != null) {
                        Toast.makeText(requireContext(), "Failed to load event", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (doc == null || !doc.exists()) {
                        Toast.makeText(requireContext(), "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Event e = Event.addEventDetailsFromSnapShot(doc);
                    currentQrCode = e.getQrCode();
                    bindEventToUi(e, doc);
                });
    }

    /**
     * Binds a loaded {@link Event} and its backing document to the UI fields.
     * <p>
     * This method:
     * <ul>
     *     <li>Formats and displays the event date and time.</li>
     *     <li>Sets text fields such as name, location, description, total spots, waitlist size, and enrolled count.</li>
     *     <li>Applies the {@code isGeolocationEnabled} state to the switch.</li>
     *     <li>Configures the poster text view and {@link ImageHandler} with existing image info.</li>
     * </ul>
     *
     * @param e   the event model built from the Firestore document
     * @param doc the raw document snapshot (used for fields not in the model)
     */
    private void bindEventToUi(@NonNull Event e, @NonNull DocumentSnapshot doc) {
        if (binding == null) return;
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

        Boolean geo = doc.getBoolean("isGeolocationEnabled");
        if (geo != null) binding.switchGeolocation.setChecked(geo);

        String imageUrl = doc.getString("imageUrl");
        String imagePath = doc.getString("imagePath");
        imageHandler.setExistingImage(imageUrl, imagePath);
        if (imagePath != null && !imagePath.isEmpty()) {
            posterTextView.setText(imagePath);
            posterTextView.setVisibility(View.VISIBLE);

        } else {
            posterTextView.setVisibility(View.GONE);
        }
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
     * This cleans up resources when the view is destroyed:
     * <ul>
     *     <li>Removes the Firestore snapshot listener, if any.</li>
     *     <li>Cancels any in-flight image upload via {@link ImageHandler}.</li>
     *     <li>Clears the view binding reference to avoid memory leaks.</li>
     * </ul>
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (eventRegistration != null) {
            eventRegistration.remove();
            eventRegistration = null;
        }
        if(imageHandler != null) imageHandler.cancelUpload(null);
        binding = null;
    }
}

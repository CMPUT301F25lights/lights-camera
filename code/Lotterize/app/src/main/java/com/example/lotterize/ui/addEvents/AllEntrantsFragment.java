package com.example.lotterize.ui.addEvents;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotterize.CurrentUser;
import com.example.lotterize.EventCsvExporter;
import com.example.lotterize.R;
import com.example.lotterize.databinding.FragmentAllEntrantsBinding;
import com.example.lotterize.ui.notifications.NotificationsViewModel;
import com.example.lotterize.ui.notifications.SendNotificationDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * {@code AllEntrantsFragment} shows the entrant categories for a given event and routes to detailed lists
 * or actions based on the organizer's selection.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Read the {@code eventId} from fragment arguments.</li>
 *     <li>Navigate to {@link com.example.lotterize.ui.addEvents.EntrantListFragment}
 *         or {@link com.example.lotterize.ui.addEvents.ChosenEntrantsList.ChosenEntrantsListFragment}
 *         for each entrant category.</li>
 *     <li>Open a map activity for waitlist locations.</li>
 *     <li>Open a notification dialog to send messages to specific entrant groups.</li>
 *     <li>Export the enrolled list as a CSV file using {@link EventCsvExporter}.</li>
 * </ul>
 */
public class AllEntrantsFragment extends Fragment {

    private FragmentAllEntrantsBinding binding;
    private FirebaseFirestore db;
    private String eventId;

    /**
     * Shared {@link NotificationsViewModel} used to send in-app notifications
     * to groups of entrants based on their status.
     */
    private NotificationsViewModel viewModel;

    /**
     * Activity Result API launcher which:
     * <ol>
     *     <li>Prompts the user to choose where to create a CSV file via SAF.</li>
     *     <li>Builds the enrolled CSV using {@link EventCsvExporter}.</li>
     *     <li>Writes the CSV content to the user-chosen URI.</li>
     * </ol>
     */    private final ActivityResultLauncher<Intent> createCsvLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) {
                    Toast.makeText(requireContext(),"Export cancelled",Toast.LENGTH_SHORT).show();
                    return;
                }

                // Retrieve the Uri to the user-chosen file (this is where we will write)
                Uri uri = result.getData().getData();
                if (uri == null) {
                    Toast.makeText(requireContext(),"No File URI",Toast.LENGTH_SHORT).show();
                    return;
                }

                // Build CSV for enrolled entrants and write it to the chosen URI
                EventCsvExporter.buildEnrolledCsv(db, eventId,
                        new EventCsvExporter.Callback() {
                            @Override public void onSuccess(@NonNull String csv) {
                                boolean ok = writeCsvStringToUri(uri, csv);
                                Toast.makeText(requireContext(),ok ? "CSV exported" : "Failed to export csv file",Toast.LENGTH_SHORT).show();
                            }
                            @Override public void onError(@NonNull String message) {
                                Toast.makeText(requireContext(),message,Toast.LENGTH_SHORT).show();
                            }
                        });
            });
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
        binding = FragmentAllEntrantsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * This configures UI interactions after the view is created:
     * <ul>
     *     <li>Initializes Firestore and {@link NotificationsViewModel}.</li>
     *     <li>Retrieves and validates the {@code eventId} argument.</li>
     *     <li>Wires up click handlers for each entrant category row</li>
     *     <li>Wires up click handlers for map row and notifications buttons.</li>
     *     <li>Registers a fragment result listener for notification results.</li>
     *     <li>Configures CSV export button to trigger the SAF launcher.</li>
     * </ul>
     *
     * @param v                  the root view returned by {@link #onCreateView}
     * @param savedInstanceState if non-null, this fragment is being re-created from a previous state
     */
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        viewModel = new ViewModelProvider(requireActivity()).get(NotificationsViewModel.class);

        eventId = getEventIdArg();
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(requireContext(), "Missing eventId", Toast.LENGTH_SHORT).show();
        }

        binding.backButton.setOnClickListener(view ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        // Open reusable list screen with a status flag
        binding.rowWaitlistList.setOnClickListener(view -> openList("WAITLIST"));
        binding.rowChosenList.setOnClickListener(view -> {
            if (eventId == null) {
                Toast.makeText(requireContext(), "Missing eventId", Toast.LENGTH_SHORT).show();
                return;
            }
            Bundle b = new Bundle();
            b.putString("eventId", eventId);

            NavHostFragment.findNavController(this).navigate(R.id.navigation_chosenEntrantsFragment, b);
        });
        binding.rowCancelledList.setOnClickListener(view -> openList("CANCELLED"));
        binding.rowEnrolledList.setOnClickListener(view -> openList("ENROLLED"));

        // Placeholder for map UI
        binding.rowWaitlistMap.setOnClickListener(view -> {

            if (eventId == null) {
                Toast.makeText(requireContext(), "Missing eventId", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(requireContext(), MapsActivity.class);
            // Pass eventId if needed
            intent.putExtra("eventId", eventId);
            startActivity(intent);
        });

        //Notify buttons -> open dialog
        binding.btnNotifyCancelled.setOnClickListener(v12 -> openNotifyDialog("cancelledList"));
        binding.btnNotifyChosen.setOnClickListener(v13 -> openNotifyDialog("selectedList"));
        binding.btnNotifyWaitlist.setOnClickListener(v14 -> openNotifyDialog("waitList"));

        // Listen for a result sent by a child fragment (SendNotificationDialogFragment).
        // The listener is bound to this fragment's view lifecycle.
        getChildFragmentManager().setFragmentResultListener(
                SendNotificationDialogFragment.RESULT_KEY //the channel identifier
                ,getViewLifecycleOwner(),
                (key, bundle) -> {
                    String eventId = bundle.getString(SendNotificationDialogFragment.RESULT_EVENT_ID, "");
                    String status = bundle.getString(SendNotificationDialogFragment.RESULT_STATUS, "");
                    String message = bundle.getString(SendNotificationDialogFragment.RESULT_MESSAGE, "");
                    if (eventId.isEmpty() || status.isEmpty() || message.isEmpty()) return;

                    viewModel.sendToStatus(eventId, status, message, CurrentUser.get().getUserId());
                    viewModel.toast().observe(getViewLifecycleOwner(), notify->{
                        if (notify != null){
                            Toast.makeText(requireContext(), notify, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
        );

        // Export enrolled entrants to CSV
        binding.btnExportCsv.setOnClickListener(v1 -> {
            if (eventId == null) {
                Toast.makeText(requireContext(), "Missing eventId", Toast.LENGTH_SHORT).show();
                return;
            }
            String fileName = "Enrolled List_" + "EventId: "+ eventId + "_" + new SimpleDateFormat("yyyyMMdd", Locale.CANADA).format(new Date()) + ".csv";
            Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("text/plain").putExtra(Intent.EXTRA_TITLE, fileName);

            createCsvLauncher.launch(i);

        });



        // TODOs for the bottom buttons
        binding.btnDrawReplacement.setOnClickListener(v15 -> {/* draw */});
    }

    /**
     * This navigates to the entrant list screen for the given status while
     * passing the current {@code eventId} as an argument.
     *
     * @param status
     *      The entrant status to display (e.g., {@code "WAITLIST"}, {@code "CHOSEN"})
     */
    private void openList(@NonNull String status) {
        if (eventId == null) {
            Toast.makeText(requireContext(), "Missing eventId", Toast.LENGTH_SHORT).show();
            return;
        }
        Bundle b = new Bundle();
        b.putString("eventId", eventId);
        b.putString("status", status);
        NavHostFragment.findNavController(this).navigate(R.id.navigation_entrantList, b);
    }

    /**
     * This navigates to a dialog where the organizer can send notifications to a given status group while
     * passing the current {@code eventId} as an argument.
     *
     * @param status
     *      The entrant status to display (e.g., {@code "WAITLIST"}, {@code "CHOSEN"})
     */
    private void openNotifyDialog(@NonNull String status) {
        if (eventId == null) {
            Toast.makeText(requireContext(), "Missing eventId", Toast.LENGTH_SHORT).show();
            return;
        }
        if (getChildFragmentManager().findFragmentByTag("SendNotificationDialog") == null) {
            SendNotificationDialogFragment.newInstance(eventId, status)
                    .show(getChildFragmentManager(), "SendNotificationDialog");
        }
    }

    /**
     * Writes CSV text to the given SAF {@link Uri} in UTF-8 encoding.
     *
     * @param uri     Writable document Uri (e.g., from ACTION_CREATE_DOCUMENT).
     * @param csvText CSV content to save.
     * @return true if write succeeds; false otherwise.
     */
    private boolean writeCsvStringToUri(@NonNull Uri uri, @NonNull String csvText) {
        try (OutputStream os = requireContext().getContentResolver().openOutputStream(uri)) {
            if (os == null) return false;
            os.write(csvText.getBytes(StandardCharsets.UTF_8));
            os.flush();
            return true;
        }
        catch (Exception e) {
            return false;
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
}

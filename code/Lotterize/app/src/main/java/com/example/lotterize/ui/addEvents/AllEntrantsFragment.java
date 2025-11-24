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
 * This is a fragment that shows entrant categories for a given event
 * (e.g., waitlist, chosen, cancelled, enrolled) and routes to detailed lists.
 * It reads the {@code eventId} from arguments, sets up click handlers for
 * each row, and exposes utility actions (export, notify, draw).
 */
public class AllEntrantsFragment extends Fragment {

    private FragmentAllEntrantsBinding binding;
    private FirebaseFirestore db;
    private String eventId;
    private NotificationsViewModel viewModel;

    // SAF launcher that asks the user where to save the CSV, then builds and writes it.
    private final ActivityResultLauncher<Intent> createCsvLauncher =
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

                // Build CSV
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
     * - Initializes Firestore
     * - Retrieves and validates the {@code eventId} argument
     * - Wires up click handlers for each entrant category row and toolbar back button
     * - Adds placeholder handlers for the bottom action buttons     *
     * @param v
     *      The root view returned by {@link #onCreateView}
     * @param savedInstanceState
     *      If non-null, this fragment is being re-created from a previous state
     */
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        eventId = getEventIdArg();
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(requireContext(), "Missing eventId", Toast.LENGTH_SHORT).show();
        }

        binding.backButton.setOnClickListener(view ->
                NavHostFragment.findNavController(this).popBackStack()
        );


        viewModel = new ViewModelProvider(requireActivity()).get(NotificationsViewModel.class);


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
        binding.rowWaitlistMap.setOnClickListener(view ->
                Toast.makeText(requireContext(), "Map coming soon", Toast.LENGTH_SHORT).show()
        );

        //Notify buttons -> open dialog
        binding.btnNotifyCancelled.setOnClickListener(v12 -> openNotifyDialog("cancelledList"));
        binding.btnNotifyChosen.setOnClickListener(v13 -> openNotifyDialog("selectedList"));
        binding.btnNotifyWaitlist.setOnClickListener(v14 -> openNotifyDialog("waitList"));

        // Listen for a result sent by a child fragment. We register the listener against this fragment's
        // view lifecycle so it automatically stops listening when the view is destroyed.
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
     * Writes CSV text to the given SAF {@link Uri} in UTF-8.
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

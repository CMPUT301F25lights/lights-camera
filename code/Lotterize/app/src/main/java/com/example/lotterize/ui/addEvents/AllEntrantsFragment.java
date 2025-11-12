package com.example.lotterize.ui.addEvents;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotterize.CurrentUser;
import com.example.lotterize.R;
import com.example.lotterize.databinding.FragmentAllEntrantsBinding;
import com.example.lotterize.ui.notifications.NotificationsViewModel;
import com.example.lotterize.ui.notifications.SendNotificationDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;


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


        viewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);


        // Open reusable list screen with a status flag
        binding.rowWaitlistList.setOnClickListener(view -> openList("WAITLIST"));
        binding.rowChosenList.setOnClickListener(view -> openList("CHOSEN"));
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




        // TODOs for the bottom buttons
        binding.btnExportCsv.setOnClickListener(v1 -> {/* export */});
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

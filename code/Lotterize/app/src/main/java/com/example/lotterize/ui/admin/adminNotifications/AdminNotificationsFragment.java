package com.example.lotterize.ui.admin.adminNotifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.lotterize.Notification;
import com.example.lotterize.databinding.FragmentAdminNotificationBinding;


/**
 * Fragment that displays a log of all notifications for admins.
 *
 * It shows notifications in a ListView using AdminNotificationArrayAdapter
 * and observes AdminNotificationsViewModel for changes to the notification list.
 * Tapping a row opens a details dialog for the selected notification.
 */

import java.util.ArrayList;

public class AdminNotificationsFragment extends Fragment {
    /** View binding for the notifications fragment layout. */
    private FragmentAdminNotificationBinding binding;

    private AdminNotificationsViewModel viewModel;
    private AdminNotificationArrayAdapter adminNotificationArrayAdapter;

    /**
     * This inflates the fragment layout, initializes the ListView and adapter,
     * and starts observing the notifications LiveData from the ViewModel.
     *
     * @param inflater
     *      The LayoutInflater object that can be used to inflate any views in the fragment
     * @param container
     *      If non-null, this is the parent view that the fragment's UI should be attached to
     * @param savedInstanceState
     *      If non-null, this fragment is being re-constructed from a previous saved state
     * @return
     *      Returns the root view of the fragment layout
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        viewModel = new ViewModelProvider(this).get(AdminNotificationsViewModel.class);
        binding = FragmentAdminNotificationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ListView listViewNotifications = binding.adminLogNotificationList;
        adminNotificationArrayAdapter = new AdminNotificationArrayAdapter(requireContext(), new ArrayList<Notification>()); // start empty);
        listViewNotifications.setAdapter(adminNotificationArrayAdapter);


        // Observe the notifications list
        viewModel.getNotifications().observe(
                getViewLifecycleOwner(),
                notifications -> {
                    adminNotificationArrayAdapter.updateData(new ArrayList<>(notifications));
                }
        );

        listViewNotifications.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Notification n = adminNotificationArrayAdapter.getItem(position);
                if (n == null) return;

                AdminNotificationDetailsDialog dialog =
                        AdminNotificationDetailsDialog.newInstance(n);

                dialog.show(getParentFragmentManager(), "admin_notification_details");
            }
        });
        return root;

    }


    /**
     * This clears the view binding reference when the view is destroyed
     * to avoid memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

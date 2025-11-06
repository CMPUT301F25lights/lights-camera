package com.example.lotterize.ui.notifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.lotterize.CurrentUser;
import com.example.lotterize.MainActivity;
import com.example.lotterize.Notification;
import com.example.lotterize.NotificationSender;
import com.example.lotterize.databinding.FragmentNotificationsBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * This is a fragment that displays a list of {@link Notification} items.
 * It sets up the {@link ListView} with a {@link NotificationArrayAdapter}
 * and observes {@link NotificationsViewModel} to update the UI when data changes.
 */
public class NotificationsFragment extends Fragment {

    /** View binding for the notifications fragment layout. */
    private FragmentNotificationsBinding binding;

    /** ViewModel that provides LiveData of notifications for this fragment. */
    private NotificationsViewModel viewModel;
    private NotificationArrayAdapter notificationArrayAdapter;

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

        viewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Set up the ListView and adapter
        ListView listViewNotifications = binding.listNotifications;
        notificationArrayAdapter = new NotificationArrayAdapter(requireContext(), new ArrayList<Notification>()); // start empty);
        listViewNotifications.setAdapter(notificationArrayAdapter);


        // Observe the notifications list
        viewModel.getNotifications().observe(
                getViewLifecycleOwner(),
                notifications -> {
                    notificationArrayAdapter.updateData(new ArrayList<>(notifications));
                }
        );

        return root;

    }

    /**
     * This is a helper method used for testing; it creates a sample {@link Notification}
     * and sends it using {@link NotificationSender}. Not intended for production usage.
     */
    public void testAddNotificationWithNewId(){
        Notification notification = new Notification();
        notification.setMessage("Testing!! Congratulations!!! You were selected to join the class CMPUT 301.");
        notification.setSenderId(CurrentUser.get().getUserId());
        notification.getReceiversId().add("mHKsPI0jmC34MYGubvHP");
        notification.getReceiversId().add("G3J3T5nuOY7Ml61XZleM");

        NotificationSender notificationSender = new NotificationSender();
        notificationSender.sendNotification(notification);
    }


    /**
     * This deletes a specific notification document from Firestore by id.
     * Intended for debugging.
     */
    public void deleteDB(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notifications").document("Gj8UwTLo8yLx5lP2T5wb").delete()
                .addOnSuccessListener(aVoid -> {

                })
                .addOnFailureListener(err -> {
                });

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

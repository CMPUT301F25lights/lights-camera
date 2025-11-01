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
 * Fragment for showing the notifications list.
 * - Sets up ListView + adapter using the NotificationArrayAdapter
 * - Observes the ViewModel LiveData to update UI
 */
public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private NotificationsViewModel viewModel;
    private NotificationArrayAdapter notificationArrayAdapter;

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

    public void testAddNotificationWithNewId(){
        Notification notification = new Notification();
        notification.setMessage("Testing!! Congratulations!!! You were selected to join the class CMPUT 301.");
        notification.setSenderId(CurrentUser.get().getUserId());
        notification.getReceiversId().add("mHKsPI0jmC34MYGubvHP");
        notification.getReceiversId().add("G3J3T5nuOY7Ml61XZleM");

        NotificationSender notificationSender = new NotificationSender();
        notificationSender.sendNotification(notification);
    }

    public void deleteDB(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notifications").document("Gj8UwTLo8yLx5lP2T5wb").delete()
                .addOnSuccessListener(aVoid -> {

                })
                .addOnFailureListener(err -> {
                });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

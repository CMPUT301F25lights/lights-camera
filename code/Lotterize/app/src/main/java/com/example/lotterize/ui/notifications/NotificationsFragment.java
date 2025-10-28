package com.example.lotterize.ui.notifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.lotterize.Notification;
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

        addTestNotificationToFirestore();
        return root;
    }

    private void addTestNotificationToFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        long newNotificationId = 10L;
        long senderId = 3L;
        String message = "Congratulation!!! You have been selected for the coding class 201. Please confirm attendance.";
        Timestamp time = Timestamp.now();

        ArrayList<Long> receiversId = new ArrayList<>();
        receiversId.add(1L);
        receiversId.add(2L);

        Notification testNotif = new Notification(newNotificationId, senderId, message, time, receiversId);

        db.collection("notifications")
                .add(testNotif)
                .addOnSuccessListener(docRef -> {
                    Log.d("FirestoreTest", "Added test notification with id: " + docRef.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreTest", "Failed to add test notification", e);
                });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

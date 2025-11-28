package com.example.lotterize.ui.admin.adminImages;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotterize.Event;
import com.example.lotterize.ImageHandler;
import com.example.lotterize.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminImagesFragment extends Fragment {

    private RecyclerView recyclerView;
    private AdminImageAdapter adapter;
    private ListenerRegistration listenerRegistration;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_images, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewImages);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3)); // 3 columns

        // Initialize adapter with empty lists
        adapter = new AdminImageAdapter(
                new ArrayList<>(),
                this::deleteImage
        );
        recyclerView.setAdapter(adapter);
        startRealtimeListener();

//        // load images from firestore
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        db.collection("events")
//                .whereNotEqualTo("imageUrl", null)
//                .get()
//                .addOnSuccessListener(snapshot -> {
//
//                    List<Event> events = new ArrayList<>();
//
//                    for (DocumentSnapshot doc : snapshot) {
//                        Event event = Event.addEventDetailsFromSnapShot(doc);
//                        event.setEventId(doc.getId()); // ensure ID is stored
//                        events.add(event);
//                    }
//
//                    adapter.updateData(events);
//                });
        return view;
    }
    private void startRealtimeListener() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        listenerRegistration = db.collection("events")
                .addSnapshotListener((snapshot, e) -> {

                    if (e != null || snapshot == null) {
                        Log.e("AdminImages", "Listen failed", e);
                        return;
                    }

                    List<Event> events = new ArrayList<>();

                    for (DocumentSnapshot doc : snapshot) {
                        Event event = Event.addEventDetailsFromSnapShot(doc);
                        event.setEventId(doc.getId());

                        // filter for events with images
                        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty() &&
                                event.getImagePath() != null && !event.getImagePath().isEmpty()) {
                            events.add(event);
                        }
                    }

                    adapter.updateData(events);
                });
    }

    private void deleteImage(Event event) {
        ImageHandler handler = new ImageHandler();
        handler.setExistingImage(event.getImageUrl(), event.getImagePath());

        // delete from Firebase Storage
        handler.removeImage(getContext(), () -> {

            // update Firestore fields
            FirebaseFirestore.getInstance()
                    .collection("events")
                    .document(event.getEventId())
                    .update("imageUrl", null, "imagePath", null)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(getContext(), "Image deleted", Toast.LENGTH_SHORT).show();

                        // update RecyclerView
                        adapter.removeEvent(event);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Failed to update Firestore", Toast.LENGTH_SHORT).show()
                    );
        });
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }


}

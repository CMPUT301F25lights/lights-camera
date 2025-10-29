package com.example.lotterize;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lotterize.databinding.ActivityShowListBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ActivityShowWaitingList extends AppCompatActivity {

    ActivityShowListBinding binding;

    FirebaseFirestore db;

    CollectionReference events;
    CollectionReference users;

    ArrayList<Long> usersId;
    ShowListArrayAdapter adapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityShowListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        events = db.collection("events");
        users = db.collection(("users"));
        usersId = new ArrayList<>();
        adapter = new ShowListArrayAdapter(this, usersId);

        ListView list = binding.listViewShowList;
        TextView header = binding.eventListNameText;
        Button interact = binding.interactButton;
        ImageButton back = binding.back;

        String leaveList = "Leave Waiting List";
        String joinList = "Join Waiting List";
        String field = getIntent().getStringExtra("type");
        long eventId = getIntent().getLongExtra("eventId",0);

        header.setText(R.string.waiting_list);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        interact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (interact.getText().equals(leaveList)) {
                    usersId.remove(0L); // TODO: replace 0 with userId
                    adapter.notifyDataSetChanged();
                    events.whereEqualTo("eventId", eventId).limit(1).get().addOnSuccessListener(snapshot-> {
                        DocumentReference event = snapshot.getDocuments().get(0).getReference();
                        event.update("waitList", FieldValue.arrayRemove(0)); // TODO: replace 0 with userId
                        interact.setText(joinList);
                    });
                } else {
                    usersId.add(0L); // TODO: replace 0 with userId
                    adapter.notifyDataSetChanged();
                    events.whereEqualTo("eventId", eventId).limit(1).get().addOnSuccessListener(snapshot -> {
                        DocumentReference event = snapshot.getDocuments().get(0).getReference();
                        event.update("waitList", FieldValue.arrayUnion(0)); // TODO: replace 0 with userId
                        interact.setText(leaveList);
                    });
                }
            }
        });

        list.setAdapter(adapter);

        events.whereEqualTo("eventId", eventId).limit(1).get().addOnSuccessListener(snapshot-> {
            List<Long> obtainedUserIds = (List<Long>) snapshot.getDocuments().get(0).get("waitList");
            if (obtainedUserIds != null && !obtainedUserIds.isEmpty()){
                if(obtainedUserIds.contains(0L)){ // TODO: replace 0 with userId
                    interact.setText(leaveList);
                } else {
                    interact.setText(joinList);
                }
                usersId.addAll(obtainedUserIds);
                adapter.notifyDataSetChanged();
            } else {
                interact.setText(joinList);
            }
        });

    }


}

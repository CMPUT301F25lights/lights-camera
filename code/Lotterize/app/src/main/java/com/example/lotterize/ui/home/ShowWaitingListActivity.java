package com.example.lotterize.ui.home;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lotterize.CurrentUser;
import com.example.lotterize.R;
import com.example.lotterize.databinding.ActivityShowListBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that shows the waiting list for an event
 * (eventID given in intent). Also has a button which allows current
 * user to join or leave the waiting list.
 */
public class ShowWaitingListActivity extends AppCompatActivity {

    ActivityShowListBinding binding;

    FirebaseFirestore db;

    CollectionReference events;
    CollectionReference users;

    ArrayList<String> usersId;
    ShowListArrayAdapter adapter;

    /**
     * Displays the waiting list, along with an option to
     * leave or join the list.
     * @param savedInstanceState intent contains a String in the
     *                           "eventId" field
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityShowListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        events = db.collection("events");
        users = db.collection(("users"));
        usersId = new ArrayList<>();
        adapter = new ShowListArrayAdapter(this, usersId, "users", "userId", "name");

        ListView list = binding.listViewShowList;
        TextView header = binding.eventListNameText;
        Button interact = binding.interactButton;
        ImageButton back = binding.back;

        String leaveList = "Leave Waiting List";
        String joinList = "Join Waiting List";
        String eventId = getIntent().getStringExtra("eventId");
        String userId = CurrentUser.get().getUserId();

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
                    usersId.remove(userId);
                    adapter.notifyDataSetChanged();
                    events.whereEqualTo("eventId", eventId).limit(1).get().addOnSuccessListener(snapshot-> {
                        DocumentReference event = snapshot.getDocuments().get(0).getReference();
                        event.update("waitList", FieldValue.arrayRemove(userId));
                        interact.setText(joinList);
                    });
                } else {
                    usersId.add(userId);
                    adapter.notifyDataSetChanged();
                    events.whereEqualTo("eventId", eventId).limit(1).get().addOnSuccessListener(snapshot -> {
                        DocumentReference event = snapshot.getDocuments().get(0).getReference();
                        event.update("waitList", FieldValue.arrayUnion(userId));
                        interact.setText(leaveList);
                    });
                }
            }
        });

        list.setAdapter(adapter);

        events.whereEqualTo("eventId", eventId).limit(1).get().addOnSuccessListener(snapshot-> {
            List<String> obtainedUserIds = (List<String>) snapshot.getDocuments().get(0).get("waitList");
            if (obtainedUserIds != null && !obtainedUserIds.isEmpty()){
                if(obtainedUserIds.contains(userId)){
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

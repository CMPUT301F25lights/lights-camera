package com.example.lotterize.ui.home;

import static android.view.View.INVISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lotterize.CurrentUser;
import com.example.lotterize.databinding.ActivityShowListBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserWaitListedEvents extends AppCompatActivity {
    ActivityShowListBinding binding;

    ArrayList<String> eventIds;

    FirebaseFirestore db;

    CollectionReference events;

    ShowListArrayAdapter adapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityShowListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        events = db.collection("events");
        eventIds = new ArrayList<>();
        adapter = new ShowListArrayAdapter(this,eventIds, "events", "eventId", "eventName");

        ListView list = binding.listViewShowList;
        TextView header = binding.eventListNameText;
        Button interact = binding.interactButton;
        ImageButton back = binding.back;

        String userId = CurrentUser.get().getUserId();
        list.setAdapter(adapter);

        String headerText = "WaitListed Events";
        header.setText(headerText);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        interact.setVisibility(INVISIBLE);

        list.setAdapter(adapter);

        events.whereArrayContains("waitList", userId).get().addOnSuccessListener(snapshot-> {
            List<DocumentSnapshot> obtainedEventIds = snapshot.getDocuments();
            for (DocumentSnapshot d : obtainedEventIds){
                eventIds.add(d.getString("eventId"));
            }
            adapter.notifyDataSetChanged();
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(UserWaitListedEvents.this, EventDetailsActivity.class);
                intent.putExtra("eventId", eventIds.get(position));
                startActivity(intent);
            }
        });

    }
}

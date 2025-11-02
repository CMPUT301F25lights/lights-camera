package com.example.lotterize.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.lotterize.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ShowWaitListArrayAdapter extends ArrayAdapter<String> {


    private FirebaseFirestore db;
    private CollectionReference users;
    public ShowWaitListArrayAdapter(Context context, ArrayList<String> userIds){
        super(context, 0, userIds);
        db = FirebaseFirestore.getInstance();
        users = db.collection("users");
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup
            parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.content_show_list,
                    parent, false);
        } else {
            view = convertView;
        }

        String userId = getItem(position);
        TextView entry = view.findViewById(R.id.user_name_text);

        users.whereEqualTo("userId", userId).limit(1).get().addOnSuccessListener(snapshot -> {
            DocumentSnapshot user = snapshot.getDocuments().get(0);
            entry.setText(String.format("%s (ID: %s)", user.getString("name"), user.getString("userId")));
        });

        return view;
    }
}

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

public class ShowListArrayAdapter extends ArrayAdapter<String> {


    private FirebaseFirestore db;
    private CollectionReference col;
    String idField;
    String nameField;
    public ShowListArrayAdapter(Context context, ArrayList<String> ids, String collection, String idField, String nameField){
        super(context, 0, ids);
        db = FirebaseFirestore.getInstance();
        col = db.collection(collection);
        this.idField = idField;
        this.nameField = nameField;
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

        String id = getItem(position);
        TextView entry = view.findViewById(R.id.user_name_text);

        col.whereEqualTo(idField, id).limit(1).get().addOnSuccessListener(snapshot -> {
            DocumentSnapshot entity = snapshot.getDocuments().get(0);
            entry.setText(String.format("%s (ID: %s)", entity.getString(nameField), entity.getString(idField)));
        });

        return view;
    }
}

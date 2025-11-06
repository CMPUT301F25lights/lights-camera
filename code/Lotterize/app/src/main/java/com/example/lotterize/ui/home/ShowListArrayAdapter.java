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

/**
 * Array Adapter showing a list of users and their ids.
 */
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

    /**
     * Creates a new entry that includes user name and their id.
     *
     * @param position The position of the item within the adapter's data set of the item whose view
     *        we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *        is non-null and of an appropriate type before using. If it is not possible to convert
     *        this view to display the correct data, this method can create a new view.
     *        Heterogeneous lists can specify their number of view types, so that this View is
     *        always of the right type (see {@link #getViewTypeCount()} and
     *        {@link #getItemViewType(int)}).
     * @param parent The parent that this view will eventually be attached to
     * @return View
     */
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

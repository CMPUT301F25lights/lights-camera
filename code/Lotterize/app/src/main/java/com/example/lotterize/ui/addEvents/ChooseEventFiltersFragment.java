package com.example.lotterize.ui.addEvents;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.lotterize.R;
import com.example.lotterize.ui.home.FilterFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ChooseEventFiltersFragment extends DialogFragment {
    public interface ChooseEventFiltersDialogListener {
        void addFilter(String f);
        void removeFilter(String f);
    }

    private FirebaseFirestore db;
    private CollectionReference filters;
    private ChipGroup filterList;

    private ChooseEventFiltersFragment.ChooseEventFiltersDialogListener listener;

    private ArrayList<String> currentFilters;

    public void setListener(ChooseEventFiltersFragment.ChooseEventFiltersDialogListener listener){
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.dialog_fragment_chip_group, null);
        filterList = view.findViewById(R.id.addEventFiltersChipGroup);

        db = FirebaseFirestore.getInstance();
        filters = db.collection("filters");
        currentFilters = new ArrayList<>();
        if (getArguments() != null && getArguments().getSerializable("Current Filters") != null){
            currentFilters = (ArrayList<String>) getArguments().getSerializable("Current Filters");
        }
        filters.get().addOnSuccessListener(snapshot -> {
                for (DocumentSnapshot filter : snapshot) {
                    Chip c = new Chip(requireContext());
                    String name = filter.getString("name");
                    c.setText(name);
                    c.setFocusable(true);
                    c.setCheckable(true);
                    if (name != null && currentFilters != null && currentFilters.contains(name)){
                        c.setChecked(true);
                    }
                    c.setChipBackgroundColor(new ColorStateList( new int[][]{new int[]{android.R.attr.state_checked},
                            new int[]{}}, new int[]{Color.GRAY, Color.WHITE}));
                    filterList.addView(c);

                    c.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (isChecked) {
                            listener.addFilter(name);
                        } else {
                            listener.removeFilter(name);
                        }
                    });
                }

                Chip addFilter = new Chip(requireContext());
                String addFilterText = "+ Add Filter";
                addFilter.setText(addFilterText);
                addFilter.setFocusable(true);
                filterList.addView(addFilter);

                addFilter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Enter New Filter");
                        EditText input = new EditText(getContext());
                        input.setHint("Filter Name");
                        builder.setView(input);
                        builder.setPositiveButton("OK", (dialog, which) -> {
                            String newFilter = input.getText().toString().trim();
                            if (!newFilter.isEmpty()){
                                Map<String, Object> doc = new HashMap<>();
                                doc.put("name", newFilter);
                                filters.add(doc);

                                Chip c = new Chip(requireContext());
                                c.setText(newFilter);
                                c.setFocusable(true);
                                c.setCheckable(true);
                                c.setChipBackgroundColor(new ColorStateList( new int[][]{new int[]{android.R.attr.state_checked},
                                        new int[]{}}, new int[]{Color.GRAY, Color.WHITE}));
                                filterList.addView(c);

                                c.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                    if (isChecked) {
                                        listener.addFilter(newFilter);
                                    } else {
                                        listener.removeFilter(newFilter);
                                    }
                                });
                            } else {
                                Toast.makeText(getContext(), "Field was empty", Toast.LENGTH_SHORT).show();
                            }
                        });
                        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                        builder.show();
                    }
                });

        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .setTitle("Filter Events")
                .setNegativeButton("OK", null)
                .create();
    }
}

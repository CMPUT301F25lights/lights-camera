package com.example.lotterize.ui.home;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.lotterize.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Creates dialog fragment allowing users to filter events using filters
 * and availability
 *
 */
public class FilterFragment extends DialogFragment {


    /**
     * Interface listener must implement to ensure correct functionality
     * for FilterFragment
     */
    public interface FilterFragmentsDialogListener {
        void addFilter(String f);
        void removeFilter(String f);
        void filterDate(int year, int month, int dayOfMonth);
        void resetDate();
    }

    private FirebaseFirestore db;
    private CollectionReference filters;
    private ChipGroup filterList;

    private FilterFragment.FilterFragmentsDialogListener listener;

    private ArrayList<String> currentFilters;

    public void setListener(FilterFragmentsDialogListener listener){
        this.listener = listener;
    }

    /**
     * Creates dialog fragment allowing users to filter events using filters
     *  and availability
     *  @param savedInstanceState The last saved instance state of the Fragment,
     *  or null if this is a freshly created Fragment.
     *
     *  @return returns Dialog
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.dialog_fragment_filter_events, null);
        filterList = view.findViewById(R.id.filter_list);

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
        });

        CalendarView calendar = view.findViewById(R.id.filter_calendar);
        Calendar shownDate = (Calendar) getArguments().getSerializable("date");
        if (shownDate != null) {
            calendar.setDate(shownDate.getTimeInMillis());
        }

        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                listener.filterDate(year, month, dayOfMonth);
            }
        });

        Button clearDate = view.findViewById(R.id.clear_date_button);

        clearDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.resetDate();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .setTitle("Filter Events")
                .setNegativeButton("OK", null)
                .create();
    }
}

package com.example.lotterize.ui.home;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.lotterize.R;

public class FilterFragment extends DialogFragment {
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.dialog_fragment_info, null);
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        infoText = view.findViewById(R.id.info_text);
        infoText.setText(homeViewModel.getText().getValue());
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .setTitle("Info About the Lottery")
                .setNegativeButton("OK", null)
                .create();
    }
}

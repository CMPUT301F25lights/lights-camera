package com.example.lotterize.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lotterize.AdminSignInActivity;
import com.example.lotterize.MainActivity;
import com.example.lotterize.R;
import com.google.firebase.auth.FirebaseAuth;

public class AdminProfileFragment extends Fragment {

    private TextView textGreetingAdmin;
    private Button buttonLogoutAdmin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_profile, container, false);

        textGreetingAdmin = view.findViewById(R.id.text_greeting_admin);
        buttonLogoutAdmin = view.findViewById(R.id.button_logout_admin);

        // Optional: dynamically show admin's name
        String adminName = getActivity().getIntent().getStringExtra("adminUsername");
        textGreetingAdmin.setText("Hello, " + adminName + "!");

        buttonLogoutAdmin.setOnClickListener(v -> {
            // So login isn't saved to device when we implement
            FirebaseAuth.getInstance().signOut();

            // Redirect to MainActivity and clear everything
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            // Close AdminActivity so user can’t go back with Back button
            requireActivity().finish();

        });

        return view;
    }
}

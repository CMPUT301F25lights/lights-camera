package com.example.lotterize.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.lotterize.R;
import com.example.lotterize.databinding.FragmentAdminUserDetailsBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminUserDetailsFragment extends Fragment {

    private FragmentAdminUserDetailsBinding binding;
    private FirebaseFirestore db;
    private String userId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAdminUserDetailsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }

        if (userId == null) {
            Toast.makeText(getContext(), "Error: No user selected", Toast.LENGTH_SHORT).show();
        } else {
            loadUserData(userId);
        }

        // Back button click
        binding.buttonBack.setOnClickListener(v -> {
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_admin).popBackStack();
        });

        // Hide bottom navigation bar
        if (getActivity() != null) {
            BottomNavigationView navView = getActivity().findViewById(R.id.nav_view_admin);
            if (navView != null) {
                navView.setVisibility(View.GONE);
            }
        }


        return root;
    }

    private void loadUserData(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(this::setUserDetails)
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show());
    }

    private void setUserDetails(DocumentSnapshot document) {
        if (document.exists()) {
            String name = document.getString("name");
            String email = document.getString("email");
            String phone = document.getString("phoneNumber");

            binding.textNameValue.setText(name);
            binding.textEmailValue.setText(email);
            binding.textPhoneValue.setText(phone);
        } else {
            Toast.makeText(getContext(), "User does not exist", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

package com.example.lotterize.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.lotterize.R;
import com.example.lotterize.databinding.FragmentProfileBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Show bottom navigation when on profile fragment
        showBottomNavigation();

        // Set up logout button
        binding.buttonLogout.setOnClickListener(v -> {
            if (getActivity() != null){
                getActivity().finish();
            }
        });

        // Navigate to Account Fragment when account section is clicked
        View.OnClickListener accountClickListener = v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.navigation_account);
        };

        binding.iconAccount.setOnClickListener(accountClickListener);
        binding.textAccount.setOnClickListener(accountClickListener);
        binding.textAccountDesc.setOnClickListener(accountClickListener);

        // Navigate to Event History when event history section is clicked
        View.OnClickListener eventHistoryClickListener = v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.navigation_event_history);
        };

        binding.iconEvent.setOnClickListener(eventHistoryClickListener);
        binding.textEvent.setOnClickListener(eventHistoryClickListener);
        binding.textEventDesc.setOnClickListener(eventHistoryClickListener);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Ensure bottom navigation is visible when returning to profile
        showBottomNavigation();
    }

    private void showBottomNavigation() {
        if (getActivity() != null) {
            BottomNavigationView navView = getActivity().findViewById(R.id.nav_view);
            if (navView != null) {
                navView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
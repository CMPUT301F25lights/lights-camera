package com.example.lotterize.ui.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.lotterize.MainActivity;
import com.example.lotterize.CurrentUser;
import com.example.lotterize.R;
import com.example.lotterize.databinding.FragmentProfileBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * The ProfileFragment class manages the user profile screen in the Lotterize app.
 * It displays user information, such as their username, and provides options
 * to navigate to account details, event history, delete the account, or log out.
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    // ViewModel to manage user data for the profile screen
    private ProfileViewModel profileViewModel;

    /**
     * Called to have the fragment instantiate its user interface view.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Ensure bottom navigation is visible when viewing the profile
        showBottomNavigation();

        // Observe user data changes and update greeting text based on username
        profileViewModel.getUserData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                String username = user.getUsername() != null ? user.getUsername() : "User";
                String greeting = "Hello, " + username + "!";
                binding.textGreeting.setText(greeting);
            }
        });

        // Delete Account
        binding.buttonDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to permanently delete your account? This action cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        profileViewModel.deleteAccount();
                        Toast.makeText(requireContext(), "Account deleted successfully.", Toast.LENGTH_SHORT).show();

                        // Clear the current user session and navigate to login screen
                        CurrentUser.clear();
                        Intent intent = new Intent(requireContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Logout
        binding.buttonLogout.setOnClickListener(v -> {
            if (getActivity() != null) {
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

        // Navigate to Event History Fragment when event section is clicked
        View.OnClickListener eventHistoryClickListener = v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.navigation_event_history);
        };
        binding.iconEvent.setOnClickListener(eventHistoryClickListener);
        binding.textEvent.setOnClickListener(eventHistoryClickListener);
        binding.textEventDesc.setOnClickListener(eventHistoryClickListener);

        return root;
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * Ensures that the bottom navigation is visible when returning to this screen.
     */
    @Override
    public void onResume() {
        super.onResume();
        showBottomNavigation();
    }

    /**
     * Makes the bottom navigation bar visible if it exists in the current activity.
     */
    private void showBottomNavigation() {
        if (getActivity() != null) {
            BottomNavigationView navView = getActivity().findViewById(R.id.nav_view);
            if (navView != null) {
                navView.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Clears the binding reference to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

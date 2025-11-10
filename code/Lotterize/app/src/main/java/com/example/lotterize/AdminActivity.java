package com.example.lotterize;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.lotterize.databinding.ActivityAdminBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Activity hosting all the admin side functionality:
 * Events, Images, and Profile.
 */
public class AdminActivity extends AppCompatActivity {
    private ActivityAdminBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view_admin);

        // Each menu ID here corresponds to a fragment in the navigation graph
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_admin_events,
                R.id.navigation_admin_images,
                R.id.navigation_admin_users,
                R.id.navigation_admin_profile
        ).build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_admin);
        NavigationUI.setupWithNavController(binding.navViewAdmin, navController);
    }
}

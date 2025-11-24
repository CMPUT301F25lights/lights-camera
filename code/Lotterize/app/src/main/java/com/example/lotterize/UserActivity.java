package com.example.lotterize;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.lotterize.databinding.ActivityUserBinding;
import com.example.lotterize.ui.notifications.NotificationsViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

/**
 * Activity hosting all the user side functionality.
 */
public class UserActivity extends AppCompatActivity {
    private ActivityUserBinding binding;
    private NotificationsViewModel notificationsViewModel;


    /**
     * Creates the bottom navigation taskbar, as well as
     * the navController. Initial fragment shown is home.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        notificationsViewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);
        BottomNavigationView navView = findViewById(R.id.nav_view);

        notificationsViewModel.snack().observe(this, message -> {
            if (message == null || message.isEmpty()) return;

            Snackbar bar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE);
            bar.setDuration(5000);
            bar.setAction("View", v -> {
                navView.setSelectedItemId(R.id.navigation_notifications);
            });
            bar.show();
        });

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_notifications, R.id.navigation_eventsregistered, R.id.navigation_profile, R.id.navigation_addEvents)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_user);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

}

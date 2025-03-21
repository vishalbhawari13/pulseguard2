package com.example.pulseguard.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.example.pulseguard.R;
import com.example.pulseguard.databinding.ActivityDashboardBinding;
import com.example.pulseguard.fragments.HomeFragment;
import com.example.pulseguard.fragments.ProfileFragment;
import com.example.pulseguard.fragments.StatsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // View Binding Initialization
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Default fragment (Home)
        loadFragment(new HomeFragment());

        // Bottom Navigation Clicks
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (item.getItemId() == R.id.nav_stats) {
                selectedFragment = new StatsFragment();
            } else if (item.getItemId() == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });

        // Navigation Drawer Clicks (Future Expansion)
        binding.navView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_profile) {
                loadFragment(new ProfileFragment());
            }
            binding.drawerLayout.closeDrawers();
            return true;
        });
    }

    // Method to load fragments dynamically
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null; // Prevent memory leaks
    }
}

package com.example.pulseguard.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.pulseguard.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private TextView welcomeText, navUserName;
    private Button btnViewStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        // Setup Custom Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);


        // Initialize Drawer Layout & Navigation View
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
            View headerView = navigationView.getHeaderView(0);
            if (headerView != null) {
                navUserName = headerView.findViewById(R.id.nav_user_name);
            }
        }

        // Bottom Navigation Setup
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(this::onBottomNavigationItemSelected);
        }

        // Display User Info
        welcomeText = findViewById(R.id.welcome_text);
        updateUserInfo();

        // Initialize Button
        btnViewStats = findViewById(R.id.btn_stats);
        btnViewStats.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, HealthStatsActivity.class);
            startActivity(intent);
        });

        // ActionBar Drawer Toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    /**
     * Updates user info on Dashboard & Navigation Drawer.
     */
    private void updateUserInfo() {
        if (user != null) {
            String displayName = user.getDisplayName();
            welcomeText.setText("Welcome, " + (displayName != null ? displayName : "User"));
            if (navUserName != null) {
                navUserName.setText(displayName != null ? displayName : "Guest User");
            }
        } else {
            welcomeText.setText("Welcome to PulseGuard");
            if (navUserName != null) {
                navUserName.setText("Guest User");
            }
        }
    }

    /**
     * Inflate the menu for logout.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_right_menu, menu);
        return true;
    }

    /**
     * Handle action bar item clicks.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logoutUser();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handle navigation drawer item clicks.
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.nav_settings) {
            Toast.makeText(this, "Settings Clicked", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_logout) {
            logoutUser();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Handle bottom navigation item clicks.
     */
    private boolean onBottomNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.bottom_home) {
            Toast.makeText(this, "Home Clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.bottom_stats) {
            startActivity(new Intent(this, HealthStatsActivity.class));
            return true;
        }
        return false;
    }

    /**
     * Logout user and redirect to Login Screen.
     */
    private void logoutUser() {
        firebaseAuth.signOut();
        Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}

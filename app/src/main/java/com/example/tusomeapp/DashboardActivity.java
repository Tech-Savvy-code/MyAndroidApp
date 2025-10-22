package com.example.tusomeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardActivity extends BaseActivity {

    private BottomNavigationView bottomNav;
    private TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize views
        bottomNav = findViewById(R.id.bottomNav);
        tvTitle = findViewById(R.id.tvTitle);

        // Get user data from Intent
        Intent intent = getIntent();
        String userName = intent.getStringExtra("USER_NAME");
        String userRole = intent.getStringExtra("ROLE");

        // Load the default fragment (Home)
        loadFragment(new HomeFragment());
        tvTitle.setText("Dashboard");

        // Bottom Navigation item selection
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String title = "Dashboard";

            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
                title = "Home";
            } else if (itemId == R.id.nav_sessions) {
                selectedFragment = new SessionsFragment();
                title = "Sessions";
            } else if (itemId == R.id.nav_messages) {
                selectedFragment = new MessagesFragment();
                title = "Messages";
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
                title = "Profile";
            }

            // Load selected fragment
            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                tvTitle.setText(title);
            }

            return true;
        });

        // Update UI for tutor role
        if (userRole != null && userRole.equals("tutor")) {
            tvTitle.setText("Tutor Dashboard");
        }
    }

    /**
     * Replace fragment inside fragment_container
     */
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    /**
     * Logout handler
     */
    public void onLogoutClick(View view) {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}

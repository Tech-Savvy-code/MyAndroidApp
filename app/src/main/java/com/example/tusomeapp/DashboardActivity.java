package com.example.tusomeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize views
        viewPager = findViewById(R.id.viewPager);
        bottomNav = findViewById(R.id.bottomNav);

        // Get user data from Intent
        Intent intent = getIntent();
        String userName = intent.getStringExtra("USER_NAME");
        String userRole = intent.getStringExtra("ROLE");

        // Setup ViewPager with fragments
        DashboardPagerAdapter pagerAdapter = new DashboardPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setUserInputEnabled(false); // Disable swipe

        // Connect ViewPager with BottomNavigation
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                bottomNav.getMenu().getItem(position).setChecked(true);
            }
        });

        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                viewPager.setCurrentItem(0);
            } else if (itemId == R.id.nav_sessions) {
                viewPager.setCurrentItem(1);
            } else if (itemId == R.id.nav_messages) {
                viewPager.setCurrentItem(2);
            } else if (itemId == R.id.nav_profile) {
                viewPager.setCurrentItem(3);
            }
            return true;
        });

        // Update UI based on user role
        if (userRole != null && userRole.equals("tutor")) {
            TextView title = findViewById(R.id.tvTitle);
            title.setText("Tutor Dashboard");
        }
    }

    // ViewPager Adapter
    private static class DashboardPagerAdapter extends FragmentStateAdapter {
        public DashboardPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new HomeFragment();
                case 1: return new SessionsFragment();
                case 2: return new MessagesFragment();
                case 3: return new ProfileFragment();
                default: return new HomeFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }

    public void onLogoutClick(View view) {
        // Clear session and return to login
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
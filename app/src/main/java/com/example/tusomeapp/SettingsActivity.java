package com.example.tusomeapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat darkModeSwitch, notificationSwitch;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Setup toolbar with back arrow
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        // Initialize SharedPreferences
        prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);

        // Initialize switches
        darkModeSwitch = findViewById(R.id.switchDarkMode);
        notificationSwitch = findViewById(R.id.switchNotifications);

        // Restore saved preferences
        boolean isDarkModeOn = prefs.getBoolean("DarkMode", false);
        boolean isNotificationOn = prefs.getBoolean("Notifications", true);

        darkModeSwitch.setChecked(isDarkModeOn);
        notificationSwitch.setChecked(isNotificationOn);

        // Apply current theme
        if (isDarkModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // 🔆 Dark Mode switch listener
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("DarkMode", isChecked).apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                Toast.makeText(this, "Dark mode enabled 🌙", Toast.LENGTH_SHORT).show();
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                Toast.makeText(this, "Dark mode disabled ☀️", Toast.LENGTH_SHORT).show();
            }
        });

        // 🔔 Notifications switch listener
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("Notifications", isChecked).apply();

            if (isChecked) {
                Toast.makeText(this, "Notifications enabled 🔔", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notifications disabled 🔕", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Handle toolbar back arrow
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

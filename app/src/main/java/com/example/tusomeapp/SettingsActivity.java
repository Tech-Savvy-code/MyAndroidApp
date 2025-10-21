package com.example.tusomeapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat darkModeSwitch, notificationSwitch;
    private SharedPreferences prefs;

    private static final String CHANNEL_ID = "tusome_notifications";

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

        // Apply saved dark mode immediately
        AppCompatDelegate.setDefaultNightMode(
                isDarkModeOn ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        // 🔹 Dark Mode switch listener
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("DarkMode", isChecked).apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                Toast.makeText(this, "Dark Mode Enabled", Toast.LENGTH_SHORT).show();
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                Toast.makeText(this, "Dark Mode Disabled", Toast.LENGTH_SHORT).show();
            }
        });

        // 🔹 Notification switch listener
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("Notifications", isChecked).apply();

            if (isChecked) {
                // Request permission for Android 13+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                            != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(
                                new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                                1
                        );
                        return;
                    }
                }

                // Create notification channel and show test notification
                createNotificationChannel();
                showTestNotification();
                Toast.makeText(this, "Notifications Enabled", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "Notifications Disabled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔹 Create a Notification Channel (required for Android 8+)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Tusome Notifications";
            String description = "Channel for Tusome App notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // 🔹 Show a sample notification when enabled
    private void showTestNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // make sure this drawable exists
                .setContentTitle("Tusome App")
                .setContentText("Notifications are now enabled 🎉")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(1001, builder.build());
        }
    }

    // 🔹 Handle toolbar back arrow
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

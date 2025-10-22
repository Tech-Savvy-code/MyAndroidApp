package com.example.tusomeapp;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Force Light Mode (ignores system dark mode)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(R.layout.activity_main);

        // Get views
        CircularProgressBar circularProgressBar = findViewById(R.id.circularProgressBar);
        TextView welcomeText = findViewById(R.id.welcome_text);
        TextView additionalText = findViewById(R.id.additional_text);
        Button skipButton = findViewById(R.id.skipButton);

        // Initially hide welcome text
        welcomeText.setAlpha(0f);

        // Animate the circular progress bar
        ValueAnimator progressAnimator = ValueAnimator.ofFloat(0, 100);
        progressAnimator.setDuration(3000);
        progressAnimator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            if (circularProgressBar != null) {
                circularProgressBar.setProgress(progress);
            }
        });

        // Fade-in animation for welcome text
        new Handler().postDelayed(() -> {
            welcomeText.animate()
                    .alpha(1f)
                    .setDuration(1000)
                    .start();
        }, 500);

        // Blinking animation for additional text
        ValueAnimator blinkAnimator = ValueAnimator.ofInt(
                0xFF616161, // gray
                0xFF6200EE  // blue
        );
        blinkAnimator.setEvaluator(new ArgbEvaluator());
        blinkAnimator.setDuration(500);
        blinkAnimator.setRepeatCount(ValueAnimator.INFINITE);
        blinkAnimator.setRepeatMode(ValueAnimator.REVERSE);
        blinkAnimator.addUpdateListener(animation -> {
            int color = (int) animation.getAnimatedValue();
            additionalText.setTextColor(color);
        });

        // Start the blinking animation
        blinkAnimator.start();

        // Skip button click listener
        skipButton.setOnClickListener(v -> navigateToLogin());

        // Start the progress animation
        progressAnimator.start();

        // Navigate to the next page after 3 seconds
        new Handler().postDelayed(this::navigateToLogin, 3000);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}

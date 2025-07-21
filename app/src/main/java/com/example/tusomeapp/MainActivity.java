package com.example.tusomeapp;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get views
        CircularProgressBar circularProgressBar = findViewById(R.id.circularProgressBar);
        TextView welcomeText = findViewById(R.id.welcome_text);
        TextView additionalText = findViewById(R.id.additional_text); // Get the additional_text TextView
        Button skipButton = findViewById(R.id.skipButton);

        // Animate the circular progress bar
        ValueAnimator progressAnimator = ValueAnimator.ofFloat(0, 100);
        progressAnimator.setDuration(3000); // 3 seconds
        progressAnimator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            circularProgressBar.setProgress(progress); // Ensure this method exists in CircularProgressBar
        });

        // Fade-in animation for welcome text
        welcomeText.animate()
                .alpha(1f) // Fade in to fully visible
                .setDuration(1000) // 1 second
                .start();

        // Blinking animation for additional text
        ValueAnimator blinkAnimator = ValueAnimator.ofInt(
                0xFF616161, // Original color (#616161)
                0xFF0000FF  // Blue color (#0000FF)
        );
        blinkAnimator.setEvaluator(new ArgbEvaluator());
        blinkAnimator.setDuration(500); // Duration for each color change
        blinkAnimator.setRepeatCount(ValueAnimator.INFINITE); // Repeat infinitely
        blinkAnimator.setRepeatMode(ValueAnimator.REVERSE); // Reverse the animation
        blinkAnimator.addUpdateListener(animation -> {
            int color = (int) animation.getAnimatedValue();
            additionalText.setTextColor(color); // Update the text color
        });

        // Start the blinking animation
        blinkAnimator.start();

        // Skip button click listener
        skipButton.setOnClickListener(v -> {

            Intent intent = new Intent(MainActivity.this, LoginActivity.class); // Replace with your target activity
            startActivity(intent);
            finish(); // Close the splash screen
        });

        // Start the progress animation
        progressAnimator.start();

        // Navigate to the next page after 3 seconds
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class); // Replace with your target activity
            startActivity(intent);
            finish(); // Close the splash screen
        }, 3000); // 3 seconds delay
    }
}
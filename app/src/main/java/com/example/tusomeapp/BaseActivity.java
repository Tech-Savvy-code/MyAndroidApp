package com.example.tusomeapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    private LinearLayout noInternetLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Automatically check and show/hide No Internet layout
        View view = findViewById(android.R.id.content);
        noInternetLayout = view.findViewById(R.id.noInternetLayout);

        checkInternetAndUpdateUI();
    }

    private void checkInternetAndUpdateUI() {
        boolean isConnected = NetworkUtils.isNetworkAvailable(this);

        if (noInternetLayout != null) {
            noInternetLayout.setVisibility(isConnected ? View.GONE : View.VISIBLE);

            if (!isConnected) {
                Button retryButton = noInternetLayout.findViewById(R.id.btnRetry);
                retryButton.setOnClickListener(v -> checkInternetAndUpdateUI());
            }
        }
    }
}

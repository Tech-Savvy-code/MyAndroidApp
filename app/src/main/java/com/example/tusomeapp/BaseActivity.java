package com.example.tusomeapp;

import android.content.Intent;
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
        checkInternetAndUpdateUI();
    }

    private void checkInternetAndUpdateUI() {
        boolean isConnected = NetworkUtils.isNetworkAvailable(this);

        View rootView = findViewById(android.R.id.content);
        noInternetLayout = rootView.findViewById(R.id.noInternetLayout);

        if (noInternetLayout != null) {
            if (isConnected) {
                noInternetLayout.setVisibility(View.GONE);
            } else {
                noInternetLayout.setVisibility(View.VISIBLE);

                Button retryButton = noInternetLayout.findViewById(R.id.btnRetry);
                retryButton.setOnClickListener(v -> checkInternetAndUpdateUI());
            }
        } else {
            // If layout not found, and not on MainActivity, go back to it
            if (!isConnected && !(this instanceof MainActivity)) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        }
    }
}

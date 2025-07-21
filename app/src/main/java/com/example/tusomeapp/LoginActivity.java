package com.example.tusomeapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        // Set up real-time validation
        setupTextWatchers();

        // Login button click listener
        btnLogin.setOnClickListener(v -> {
            if (validateInputs()) {
                simulateLogin();
            }
        });
    }

    private void setupTextWatchers() {
        // Email field validation while typing
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmail();
                updateLoginButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Password field validation while typing
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword();
                updateLoginButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Input validation
    private boolean validateInputs() {
        boolean isEmailValid = validateEmail();
        boolean isPasswordValid = validatePassword();
        return isEmailValid && isPasswordValid;
    }

    private boolean validateEmail() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email (e.g., user@example.com)");
            return false;
        } else {
            etEmail.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String password = etPassword.getText().toString().trim();
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            return false;
        } else if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return false;
        } else {
            etPassword.setError(null);
            return true;
        }
    }

    private void updateLoginButtonState() {
        btnLogin.setEnabled(validateEmail() && validatePassword());
    }
    public void onSignupClick(View view) {
        startActivity(new Intent(this, SignupActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    // Simulate login process
    private void simulateLogin() {
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        // Mock network delay
        new Handler().postDelayed(() -> {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);

            // Successful login
            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }, 1500);
    }

}
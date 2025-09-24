package com.example.tusomeapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class SignupActivity extends AppCompatActivity {

    private TextView tvPasswordStrength;
    private ProgressBar progressBar;
    private CheckBox checkTerms;
    private EditText etName, etEmail, etPassword;
    private Button btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize views
        tvPasswordStrength = findViewById(R.id.tvPasswordStrength);
        progressBar = findViewById(R.id.progressBar);
        checkTerms = findViewById(R.id.checkTerms);
        btnSignup = findViewById(R.id.btnSignup);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        // Initially disable the signup button
        btnSignup.setEnabled(false);
        btnSignup.setAlpha(0.5f); // Visual indication for disabled state

        // Setup real-time validation
        TextWatcher validationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFormValidity();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etName.addTextChangedListener(validationWatcher);
        etEmail.addTextChangedListener(validationWatcher);
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordStrength(s.toString());
                checkFormValidity();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        checkTerms.setOnCheckedChangeListener((buttonView, isChecked) -> checkFormValidity());

        // Signup button click listener
        btnSignup.setOnClickListener(v -> {
            if (validateForm()) {
                performSignup();
            }
        });
    }

    private void checkFormValidity() {
        boolean isValid = !etName.getText().toString().trim().isEmpty() &&
                Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString().trim()).matches() &&
                etPassword.getText().toString().trim().length() >= 6 &&
                checkTerms.isChecked();

        btnSignup.setEnabled(isValid);
        btnSignup.setAlpha(isValid ? 1f : 0.5f);
    }

    private void updatePasswordStrength(String password) {
        String strength;
        int color;

        if (password.isEmpty()) {
            tvPasswordStrength.setText("");
            return;
        } else if (password.length() < 6) {
            strength = "Weak";
            color = R.color.red;
        } else if (!password.matches(".*[A-Z].*") || !password.matches(".*\\d.*")) {
            strength = "Medium";
            color = R.color.orange;
        } else {
            strength = "Strong";
            color = R.color.green;
        }

        tvPasswordStrength.setText(strength);
        tvPasswordStrength.setTextColor(ContextCompat.getColor(this, color));
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Name validation
        if (etName.getText().toString().trim().isEmpty()) {
            etName.setError("Full name is required");
            isValid = false;
        }

        // Email validation
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            isValid = false;
        }

        // Password validation
        String password = etPassword.getText().toString().trim();
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            isValid = false;
        } else if (!password.matches(".*[A-Z].*") || !password.matches(".*\\d.*")) {
            etPassword.setError("Include at least one uppercase letter and one number");
            isValid = false;
        }

        // Terms validation
        if (!checkTerms.isChecked()) {
            Toast.makeText(this, "Please accept terms and conditions", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private void performSignup() {
        progressBar.setVisibility(View.VISIBLE);
        btnSignup.setEnabled(false);

        // Get user role
        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        radioGroup.getCheckedRadioButtonId();

        // Mock signup process
        new Handler().postDelayed(() -> {
            progressBar.setVisibility(View.GONE);
            btnSignup.setEnabled(true);

            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }, 2000);
    }

    public void onLoginClick(View view) {
        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
package com.example.tusomeapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";

    private EditText etName, etEmail, etPassword;
    private Button btnSignup;
    private ProgressBar progressBar;
    private CheckBox checkTerms;
    private TextView tvPasswordStrength;
    private RadioGroup radioGroup;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignup = findViewById(R.id.btnSignup);
        progressBar = findViewById(R.id.progressBar);
        checkTerms = findViewById(R.id.checkTerms);
        tvPasswordStrength = findViewById(R.id.tvPasswordStrength);
        radioGroup = findViewById(R.id.radioGroup);

        btnSignup.setEnabled(false);
        btnSignup.setAlpha(0.5f);

        setupValidationListeners();

        btnSignup.setOnClickListener(v -> {
            if (validateForm()) {
                registerUser();
            }
        });
    }

    private void setupValidationListeners() {
        TextWatcher validationWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFormValidity();
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        etName.addTextChangedListener(validationWatcher);
        etEmail.addTextChangedListener(validationWatcher);
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordStrength(s.toString());
                checkFormValidity();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        checkTerms.setOnCheckedChangeListener((buttonView, isChecked) -> checkFormValidity());
    }

    private void checkFormValidity() {
        boolean isValid = !etName.getText().toString().trim().isEmpty()
                && Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString().trim()).matches()
                && etPassword.getText().toString().trim().length() >= 6
                && checkTerms.isChecked();

        btnSignup.setEnabled(isValid);
        btnSignup.setAlpha(isValid ? 1f : 0.5f);
    }

    private void updatePasswordStrength(String password) {
        if (password.isEmpty()) {
            tvPasswordStrength.setText("");
            return;
        }

        String strength;
        int color;

        if (password.length() < 6) {
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

        if (etName.getText().toString().trim().isEmpty()) {
            etName.setError("Full name is required");
            isValid = false;
        }

        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            isValid = false;
        }

        String password = etPassword.getText().toString().trim();
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            isValid = false;
        }

        if (!checkTerms.isChecked()) {
            Toast.makeText(this, "Please accept terms and conditions", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (radioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private void registerUser() {
        progressBar.setVisibility(View.VISIBLE);
        btnSignup.setEnabled(false);

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        int selectedRoleId = radioGroup.getCheckedRadioButtonId();
        String role = (selectedRoleId == R.id.radioStudent) ? "Student" :
                (selectedRoleId == R.id.radioTutor) ? "Tutor" : "Unknown";

        Log.d(TAG, "Registering user: " + email);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user == null) {
                            Log.e(TAG, "User is null after successful creation");
                            hideProgressAndEnableButton();
                            Toast.makeText(SignupActivity.this, "Account created but user not found!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Log.d(TAG, "Firebase Auth user created successfully: " + user.getUid());

                        // Send email verification
                        user.sendEmailVerification()
                                .addOnCompleteListener(emailTask -> {
                                    if (emailTask.isSuccessful()) {
                                        Log.d(TAG, "Email verification sent.");
                                    } else {
                                        Log.e(TAG, "Failed to send email verification: " +
                                                (emailTask.getException() != null ? emailTask.getException().getMessage() : "Unknown error"));
                                    }
                                });

                        // Save user data to Firestore
                        saveUserToFirestore(user, name, email, role);

                    } else {
                        Exception e = task.getException();
                        Log.e(TAG, "Auth creation failed: " + (e != null ? e.getMessage() : "Unknown error"));
                        hideProgressAndEnableButton();
                        Toast.makeText(SignupActivity.this, "Signup failed: " + (e != null ? e.getMessage() : "Unknown error"), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(FirebaseUser user, String name, String email, String role) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("email", email);
        userMap.put("role", role);
        userMap.put("emailVerified", false);
        userMap.put("uid", user.getUid());
        userMap.put("createdAt", com.google.firebase.Timestamp.now());

        Log.d(TAG, "Saving user to Firestore: " + user.getUid());

        db.collection("users").document(user.getUid())
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "🎉 Firestore SUCCESS - User data saved!");

                    // Success flow
                    hideProgressAndEnableButton();
                    clearFields();
                    Toast.makeText(SignupActivity.this,
                            "✅ Account created successfully!",
                            Toast.LENGTH_LONG).show();

                    // Navigate to login after delay
                    new Handler().postDelayed(() -> {
                        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }, 3000);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Firestore ERROR: " + e.getMessage());

                    // Even if Firestore fails, user is still created in Auth
                    // Proceed with success but log the issue
                    hideProgressAndEnableButton();
                    clearFields();
                    Toast.makeText(SignupActivity.this,
                            "✅ Account created! Please check your email for verification. (Some data may sync later)",
                            Toast.LENGTH_LONG).show();

                    new Handler().postDelayed(() -> {
                        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }, 3000);
                });
    }

    private void hideProgressAndEnableButton() {
        progressBar.setVisibility(View.GONE);
        btnSignup.setEnabled(true);
    }

    private void clearFields() {
        etName.setText("");
        etEmail.setText("");
        etPassword.setText("");
        checkTerms.setChecked(false);
        tvPasswordStrength.setText("");
        radioGroup.clearCheck();
        checkFormValidity();
    }

    public void onLoginClick(View view) {
        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
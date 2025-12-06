package com.example.tusomeapp;

import android.content.Intent;
import android.content.SharedPreferences;
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

public class SignupActivity extends BaseActivity {

    private static final String TAG = "SignupActivity";

    private EditText etName, etEmail, etPassword;
    private Button btnSignup;
    private ProgressBar progressBar;
    private CheckBox checkTerms;
    private TextView tvPasswordStrength, tvTerms;
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
        tvTerms = findViewById(R.id.tvTerms); // NEW

        btnSignup.setEnabled(false);
        btnSignup.setAlpha(0.5f);

        // Disable checkbox until user reads Terms
        checkTerms.setEnabled(false);

        setupValidationListeners();

        // When user clicks Terms text, show dialog
        tvTerms.setOnClickListener(v -> showTermsDialog());

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

        // Prevent manual checking before reading Terms
        checkTerms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!checkTerms.isEnabled()) {
                checkTerms.setChecked(false);
                Toast.makeText(this, "Please read the Terms and Conditions first", Toast.LENGTH_SHORT).show();
                return;
            }
            checkFormValidity();
        });
    }

    private void checkFormValidity() {
        boolean isValid = !etName.getText().toString().trim().isEmpty()
                && Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString().trim()).matches()
                && etPassword.getText().toString().trim().length() >= 6
                && checkTerms.isChecked()
                && radioGroup.getCheckedRadioButtonId() != -1;

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
            etEmail.setError("Enter a valid email");
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
            Toast.makeText(this, "Please accept Terms and Conditions", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (radioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    // TERMS & CONDITIONS POPUP
    private void showTermsDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Terms and Conditions");

        builder.setMessage(
                "Welcome to Tusome.\n\n" +
                        "By creating an account you agree that:\n\n" +
                        "• You will enter accurate personal details.\n" +
                        "• You will respect tutors and fellow students.\n" +
                        "• You will not upload harmful or illegal content.\n" +
                        "• Any misconduct can lead to account suspension.\n" +
                        "• Some activity may be monitored to improve learning.\n\n" +
                        "Press ACCEPT to continue."
        );

        builder.setPositiveButton("ACCEPT", (dialog, which) -> {
            checkTerms.setEnabled(true);
            checkTerms.setChecked(true);
            checkFormValidity();
        });

        builder.setNegativeButton("DECLINE", (dialog, which) -> {
            checkTerms.setEnabled(false);
            checkTerms.setChecked(false);
            checkFormValidity();
        });

        builder.show();
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

        Log.d(TAG, "Registering: " + email);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user == null) {
                            Log.e(TAG, "User null after registration");
                            hideProgressAndEnableButton();
                            return;
                        }

                        user.sendEmailVerification();

                        saveUserToFirestore(user, name, email, role);

                    } else {
                        Exception e = task.getException();
                        Log.e(TAG, "Signup failed: " + (e != null ? e.getMessage() : "Unknown error"));
                        hideProgressAndEnableButton();
                        Toast.makeText(this, "Signup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
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

        db.collection("users").document(user.getUid())
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    saveUserToSharedPreferences(name, email, role, user.getUid());
                    hideProgressAndEnableButton();
                    clearFields();

                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_LONG).show();

                    new Handler().postDelayed(() -> {
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    }, 3000);
                })
                .addOnFailureListener(e -> {
                    saveUserToSharedPreferences(name, email, role, user.getUid());
                    hideProgressAndEnableButton();
                    clearFields();
                    Toast.makeText(this, "Account created! Verify your email.", Toast.LENGTH_LONG).show();

                    new Handler().postDelayed(() -> {
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    }, 3000);
                });
    }

    private void saveUserToSharedPreferences(String name, String email, String role, String uid) {
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("userName", name);
        editor.putString("userEmail", email);
        editor.putString("userRole", role);
        editor.putString("userId", uid);
        editor.putBoolean("isLoggedIn", true);
        editor.putLong("joinDate", System.currentTimeMillis());

        editor.apply();
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

package com.example.tusomeapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore; // ADD THIS IMPORT

public class LoginActivity extends BaseActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private TextView tvForgotPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(v -> {
            if (validateInputs()) {
                loginUser();
            }
        });

        // Forgot Password click listener
        tvForgotPassword.setOnClickListener(v -> {
            showForgotPasswordDialog();
        });
    }

    private boolean validateInputs() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return false;
        }

        return true;
    }

    private void loginUser() {
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // ✅ FIXED: Fetch actual user data from Firestore instead of using defaults
                            fetchUserDataFromFirestore(user.getUid(), user.getEmail());
                        }
                    } else {
                        String errorMessage = "Login failed";
                        if (task.getException() != null) {
                            String exceptionMessage = task.getException().getMessage();
                            if (exceptionMessage.contains("invalid-credential") ||
                                    exceptionMessage.contains("wrong-password")) {
                                errorMessage = "Invalid email or password";
                            } else if (exceptionMessage.contains("user-not-found")) {
                                errorMessage = "No account found with this email";
                            } else if (exceptionMessage.contains("network-error")) {
                                errorMessage = "Network error. Please check your connection";
                            } else {
                                errorMessage = exceptionMessage;
                            }
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ✅ NEW METHOD: Fetch actual user data from Firestore
    private void fetchUserDataFromFirestore(String userId, String userEmail) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        // User data exists in Firestore - use the actual data
                        String userName = task.getResult().getString("name");
                        String userRole = task.getResult().getString("role");
                        String userEmailFromDB = task.getResult().getString("email");

                        // Use data from Firestore, fallback to defaults if needed
                        saveUserToSharedPreferences(
                                userName != null ? userName : "User Name",
                                userEmailFromDB != null ? userEmailFromDB : userEmail,
                                userRole != null ? userRole : "Student",
                                userId
                        );

                        clearFields();
                        Toast.makeText(this, "Welcome back " + (userName != null ? userName : userEmail), Toast.LENGTH_SHORT).show();

                        new Handler().postDelayed(() -> {
                            startActivity(new Intent(this, DashboardActivity.class));
                            finish();
                        }, 2000);

                    } else {
                        // User data doesn't exist in Firestore - use basic info from Auth
                        saveUserToSharedPreferences("User Name", userEmail, "Student", userId);

                        clearFields();
                        Toast.makeText(this, "Welcome back " + userEmail, Toast.LENGTH_SHORT).show();

                        new Handler().postDelayed(() -> {
                            startActivity(new Intent(this, DashboardActivity.class));
                            finish();
                        }, 2000);
                    }
                })
                .addOnFailureListener(e -> {
                    // If Firestore fails, still proceed with basic Auth data
                    saveUserToSharedPreferences("User Name", userEmail, "Student", userId);

                    clearFields();
                    Toast.makeText(this, "Welcome back " + userEmail, Toast.LENGTH_SHORT).show();

                    new Handler().postDelayed(() -> {
                        startActivity(new Intent(this, DashboardActivity.class));
                        finish();
                    }, 2000);
                });
    }

    private void showForgotPasswordDialog() {
        // Pre-fill with email from login field if available
        String currentEmail = etEmail.getText().toString().trim();

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Reset Password");
        builder.setMessage("Enter your email address to receive a password reset link");

        // Set up the input
        final EditText input = new EditText(this);
        input.setHint("Email address");
        input.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        if (!currentEmail.isEmpty()) {
            input.setText(currentEmail);
        }

        // Add padding to the input field
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(50, 20, 50, 20);
        input.setLayoutParams(params);
        container.addView(input);

        builder.setView(container);

        // Set up the buttons
        builder.setPositiveButton("Send Reset Link", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (!email.isEmpty()) {
                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    sendPasswordResetEmail(email);
                } else {
                    Toast.makeText(LoginActivity.this, "Please enter a valid email address", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(LoginActivity.this, "Please enter your email address", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void sendPasswordResetEmail(String email) {
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);

                    if (task.isSuccessful()) {
                        showResetSuccessDialog(email);
                    } else {
                        String errorMessage = "Failed to send reset email";
                        if (task.getException() != null) {
                            String exceptionMessage = task.getException().getMessage();
                            if (exceptionMessage.contains("user-not-found")) {
                                errorMessage = "No account found with this email address";
                            } else if (exceptionMessage.contains("invalid-email")) {
                                errorMessage = "Invalid email address format";
                            } else if (exceptionMessage.contains("network-error")) {
                                errorMessage = "Network error. Please check your internet connection";
                            } else {
                                errorMessage = exceptionMessage;
                            }
                        }

                        Toast.makeText(LoginActivity.this,
                                "Error: " + errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this,
                            "Failed to send reset email. Please try again.",
                            Toast.LENGTH_LONG).show();
                });
    }

    private void showResetSuccessDialog(String email) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Check Your Email");
        builder.setMessage("We've sent a password reset link to:\n\n" + email + "\n\n" +
                "Please check your inbox and also your spam folder.\n\n" +
                "The email might take a few minutes to arrive.");

        builder.setPositiveButton("OK", (dialog, which) -> {
            // Optionally clear the dialog
        });

        builder.setNegativeButton("Resend", (dialog, which) -> {
            // Resend the reset email
            sendPasswordResetEmail(email);
        });

        builder.setCancelable(false);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Save user data to SharedPreferences (same as in SignupActivity)
    private void saveUserToSharedPreferences(String name, String email, String role, String uid) {
        android.content.SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();

        editor.putString("userName", name != null ? name : "User Name");
        editor.putString("userEmail", email != null ? email : "user@example.com");
        editor.putString("userRole", role != null ? role : "Student");
        editor.putString("userId", uid != null ? uid : "TUS001");
        editor.putLong("joinDate", System.currentTimeMillis());
        editor.putBoolean("isLoggedIn", true);

        editor.apply();

        // Log for debugging
        android.util.Log.d("LoginActivity", "✅ User data saved to SharedPreferences: " + name + " | " + email + " | " + role);
    }

    private void clearFields() {
        etEmail.setText("");
        etPassword.setText("");
    }

    public void onSignupClick(View view) {
        startActivity(new Intent(this, SignupActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
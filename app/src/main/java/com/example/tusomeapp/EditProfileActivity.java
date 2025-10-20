package com.example.tusomeapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editName, editEmail, editOldPassword, editNewPassword;
    private Button btnSave;
    private ImageView profileImage;
    private TextView tvChangePhoto;
    private Uri imageUri;
    private ProgressDialog progressDialog;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private StorageReference storageRef;

    // Image picker
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        profileImage.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editOldPassword = findViewById(R.id.editOldPassword);
        editNewPassword = findViewById(R.id.editNewPassword);
        btnSave = findViewById(R.id.btnSave);
        profileImage = findViewById(R.id.profileImage);
        tvChangePhoto = findViewById(R.id.tvChangePhoto);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating profile...");
        progressDialog.setCancelable(false);

        // Firebase init
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");

        loadUserData();
        tvChangePhoto.setOnClickListener(v -> openImagePicker());
        setupValidationListeners();

        btnSave.setOnClickListener(v -> {
            if (validateFields()) {
                updateProfile();
            } else {
                Toast.makeText(this, "⚠️ Please correct highlighted errors", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void loadUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editEmail.setText(user.getEmail());

        DocumentReference docRef = firestore.collection("users").document(user.getUid());
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                editName.setText(documentSnapshot.getString("name"));
                String imageUrl = documentSnapshot.getString("imageUrl");
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    // Use your preferred image loader (e.g. Glide)
                    // Glide.with(this).load(imageUrl).into(profileImage);
                }
            }
        });
    }

    private void setupValidationListeners() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { validateFields(); }
        };

        editName.addTextChangedListener(watcher);
        editEmail.addTextChangedListener(watcher);
        editOldPassword.addTextChangedListener(watcher);
        editNewPassword.addTextChangedListener(watcher);
    }

    private boolean validateFields() {
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String oldPass = editOldPassword.getText().toString().trim();
        String newPass = editNewPassword.getText().toString().trim();
        boolean valid = true;

        // Name
        if (TextUtils.isEmpty(name)) {
            editName.setError("Full name is required");
            valid = false;
        } else if (name.length() < 3) {
            editName.setError("Name too short");
            valid = false;
        } else {
            editName.setError(null);
        }

        // Email
        if (TextUtils.isEmpty(email)) {
            editEmail.setError("Email is required");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Invalid email");
            valid = false;
        } else {
            editEmail.setError(null);
        }

        // Password validation
        if (!TextUtils.isEmpty(oldPass) || !TextUtils.isEmpty(newPass)) {
            if (TextUtils.isEmpty(oldPass)) {
                editOldPassword.setError("Old password required");
                valid = false;
            } else {
                editOldPassword.setError(null);
            }

            if (TextUtils.isEmpty(newPass)) {
                editNewPassword.setError("New password required");
                valid = false;
            } else if (newPass.length() < 6) {
                editNewPassword.setError("Min 6 characters");
                valid = false;
            } else {
                editNewPassword.setError(null);
            }
        }

        btnSave.setEnabled(valid);
        return valid;
    }

    private void updateProfile() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String oldPass = editOldPassword.getText().toString().trim();
        String newPass = editNewPassword.getText().toString().trim();

        // Update email if changed
        user.updateEmail(email).addOnSuccessListener(aVoid -> {
            // Update password if provided
            if (!TextUtils.isEmpty(oldPass) && !TextUtils.isEmpty(newPass)) {
                user.updatePassword(newPass).addOnSuccessListener(aVoid1 -> {
                    uploadProfileData(user.getUid(), name, email);
                }).addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Password update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            } else {
                uploadProfileData(user.getUid(), name, email);
            }
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Email update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void uploadProfileData(String uid, String name, String email) {
        if (imageUri != null) {
            StorageReference fileRef = storageRef.child(uid + ".jpg");
            UploadTask uploadTask = fileRef.putFile(imageUri);

            uploadTask.addOnSuccessListener(taskSnapshot ->
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        saveToFirestore(uid, name, email, uri.toString());
                    })).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        } else {
            saveToFirestore(uid, name, email, null);
        }
    }

    private void saveToFirestore(String uid, String name, String email, String imageUrl) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("email", email);
        if (imageUrl != null) userMap.put("imageUrl", imageUrl);

        firestore.collection("users").document(uid)
                .set(userMap)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "✅ Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}

package com.example.tusomeapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.io.IOException;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editName, editEmail, editOldPassword, editNewPassword;
    private Button btnSave;
    private ImageView profileImage;
    private TextView tvChangePhoto;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editOldPassword = findViewById(R.id.editOldPassword);
        editNewPassword = findViewById(R.id.editNewPassword);
        btnSave = findViewById(R.id.btnSave);
        profileImage = findViewById(R.id.profileImage);
        tvChangePhoto = findViewById(R.id.tvChangePhoto);

        loadExistingData();

        tvChangePhoto.setOnClickListener(v -> openImagePicker());
        btnSave.setOnClickListener(v -> validateAndSave());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadExistingData() {
        SharedPreferences prefs = getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        editName.setText(prefs.getString("userName", ""));
        editEmail.setText(prefs.getString("userEmail", ""));
    }

    private void validateAndSave() {
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String oldPass = editOldPassword.getText().toString().trim();
        String newPass = editNewPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Invalid email format");
            return;
        }

        if (!TextUtils.isEmpty(newPass) && TextUtils.isEmpty(oldPass)) {
            Toast.makeText(this, "Enter your old password to set a new one", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences.Editor editor = getSharedPreferences("UserProfile", Context.MODE_PRIVATE).edit();
        editor.putString("userName", name);
        editor.putString("userEmail", email);
        if (!TextUtils.isEmpty(newPass)) {
            editor.putString("userPassword", newPass);
        }
        if (imageUri != null) {
            editor.putString("profileImageUri", imageUri.toString());
        }
        editor.apply();

        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}

package com.example.tusomeapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private static final int PICK_IMAGE_REQUEST = 1;

    private CircleImageView profileImage;
    private ImageButton editProfileImageButton;
    private TextView userNameText, userEmailText, userRoleText;
    private TextView memberSinceText, userIdText, accountStatusText;
    private TextView totalSessionsText, learningHoursText, successRateText, currentStreakText;
    private LinearLayout editProfileButton, changePasswordButton, settingsButton, logoutButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize all views
        initializeViews(view);
        setupClickListeners();
        loadUserProfileData();
        loadProfilePicture();

        return view;
    }

    // ✅ ADD THIS: Refresh profile when fragment becomes visible
    @Override
    public void onResume() {
        super.onResume();
        loadUserProfileData();
        Log.d(TAG, "ProfileFragment resumed - refreshing data");
    }

    private void initializeViews(View view) {
        profileImage = view.findViewById(R.id.profileImage);
        editProfileImageButton = view.findViewById(R.id.editProfileImageButton);

        userNameText = view.findViewById(R.id.userNameText);
        userEmailText = view.findViewById(R.id.userEmailText);
        userRoleText = view.findViewById(R.id.userRoleText);

        memberSinceText = view.findViewById(R.id.memberSinceText);
        userIdText = view.findViewById(R.id.userIdText);
        accountStatusText = view.findViewById(R.id.accountStatusText);

        totalSessionsText = view.findViewById(R.id.totalSessionsText);
        learningHoursText = view.findViewById(R.id.learningHoursText);
        successRateText = view.findViewById(R.id.successRateText);
        currentStreakText = view.findViewById(R.id.currentStreakText);

        editProfileButton = view.findViewById(R.id.editProfileButton);
        changePasswordButton = view.findViewById(R.id.changePasswordButton);
        settingsButton = view.findViewById(R.id.settingsButton);
        logoutButton = view.findViewById(R.id.logoutButton);
    }

    private void setupClickListeners() {
        // Edit profile picture
        editProfileImageButton.setOnClickListener(v -> openImagePicker());
        profileImage.setOnClickListener(v -> openImagePicker());

        // Action buttons
        editProfileButton.setOnClickListener(v -> showEditProfileDialog());
        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());
        settingsButton.setOnClickListener(v -> openSettings());
        logoutButton.setOnClickListener(v -> logoutUser());
    }

    private void loadUserProfileData() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE);

        // Check if user is logged in
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (!isLoggedIn) {
            Toast.makeText(getContext(), "Please log in to view profile", Toast.LENGTH_SHORT).show();
            // Clear profile data if not logged in
            clearProfileData();
            return;
        }

        // Load user data from SharedPreferences (set during signup/login)
        String userName = prefs.getString("userName", "User Name");
        String userEmail = prefs.getString("userEmail", "user@example.com");
        String userRole = prefs.getString("userRole", "Student");
        String userId = prefs.getString("userId", "TUS001");
        long joinDate = prefs.getLong("joinDate", System.currentTimeMillis());

        // Update UI with user data
        userNameText.setText(userName);
        userEmailText.setText(userEmail);
        userRoleText.setText(userRole);

        // Format join date
        String formattedDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date(joinDate));
        memberSinceText.setText("Member since: " + formattedDate);

        // Set user ID
        userIdText.setText("ID: " + userId);

        // Set account status based on verification (you can enhance this later)
        accountStatusText.setText("Account Status: Active");

        // Set demo statistics (you can replace with real data later)
        setDemoStatistics();

        Log.d(TAG, "✅ Profile loaded: " + userName + " | " + userEmail + " | " + userRole);
    }

    // ✅ ADD THIS: Clear profile data when not logged in
    private void clearProfileData() {
        userNameText.setText("User Name");
        userEmailText.setText("user@example.com");
        userRoleText.setText("Student");
        memberSinceText.setText("Member since: --");
        userIdText.setText("ID: --");
        accountStatusText.setText("Account Status: Not logged in");
    }

    private String generateUserId(String email) {
        // Simple ID generation from email
        if (email.contains("@")) {
            String prefix = email.substring(0, email.indexOf('@')).toUpperCase();
            return "TUS" + Math.abs(prefix.hashCode() % 1000);
        }
        return "TUS001";
    }

    private void setDemoStatistics() {
        // Demo data for presentation - replace with real data later
        totalSessionsText.setText("12");
        learningHoursText.setText("24h");
        successRateText.setText("92%");
        currentStreakText.setText("5 days");
    }

    private void loadProfilePicture() {
        // Load saved profile picture if exists
        File imageFile = new File(requireContext().getFilesDir(), "profile_picture.jpg");
        if (imageFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            profileImage.setImageBitmap(bitmap);
        }
        // Otherwise, the default image from XML will be used
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), selectedImageUri);
                profileImage.setImageBitmap(bitmap);
                saveProfilePicture(bitmap);
                Toast.makeText(getContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveProfilePicture(Bitmap bitmap) {
        try {
            File file = new File(requireContext().getFilesDir(), "profile_picture.jpg");
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showEditProfileDialog() {
        Toast.makeText(getContext(), "Edit Profile feature coming soon!", Toast.LENGTH_SHORT).show();
        // You can implement a dialog to edit name, email, etc.
    }

    private void showChangePasswordDialog() {
        Toast.makeText(getContext(), "Change Password feature coming soon!", Toast.LENGTH_SHORT).show();
        // Implement password change functionality
    }

    private void openSettings() {
        Toast.makeText(getContext(), "Settings feature coming soon!", Toast.LENGTH_SHORT).show();
        // Open settings activity or dialog
    }

    private void logoutUser() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear(); // ✅ Clear all user data on logout
        editor.apply();

        // Also delete profile picture
        File imageFile = new File(requireContext().getFilesDir(), "profile_picture.jpg");
        if (imageFile.exists()) {
            imageFile.delete();
        }

        // Navigate to login screen
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        startActivity(intent);
        requireActivity().finish();

        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
}
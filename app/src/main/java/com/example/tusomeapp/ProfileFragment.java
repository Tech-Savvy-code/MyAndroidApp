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
    private static final int PICK_IMAGE_REQUEST =  1;

    private CircleImageView profileImage;
    private ImageButton editProfileImageButton;
    private TextView userNameText, userEmailText, userRoleText;
    private TextView memberSinceText, userIdText, accountStatusText;
    private TextView totalSessionsText, learningHoursText, successRateText, currentStreakText;
    private LinearLayout editProfileButton, changePasswordButton, settingsButton, logoutButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initializeViews(view);
        setupClickListeners();
        loadUserProfileData();
        loadProfilePicture();

        return view;
    }

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
        editProfileImageButton.setOnClickListener(v -> openImagePicker());
        profileImage.setOnClickListener(v -> openImagePicker());

        editProfileButton.setOnClickListener(v -> showEditProfileDialog());
        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());
        settingsButton.setOnClickListener(v -> openSettings());
        logoutButton.setOnClickListener(v -> logoutUser());
    }

    private void loadUserProfileData() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (!isLoggedIn) {
            Toast.makeText(getContext(), "Please log in to view your profile", Toast.LENGTH_SHORT).show();
            clearProfileData();
            return;
        }

        String userName = prefs.getString("userName", "User Name");
        String userEmail = prefs.getString("userEmail", "user@example.com");
        String userRole = prefs.getString("userRole", "Student");
        String userId = prefs.getString("userId", "TUS001");
        long joinDate = prefs.getLong("joinDate", System.currentTimeMillis());

        userNameText.setText(userName);
        userEmailText.setText(userEmail);
        userRoleText.setText(userRole);

        String formattedDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date(joinDate));
        memberSinceText.setText(formattedDate);
        userIdText.setText(userId);
        accountStatusText.setText("Verified");

        setDemoStatistics();

        Log.d(TAG, "✅ Profile loaded: " + userName + " | " + userEmail + " | " + userRole);
    }

    private void clearProfileData() {
        userNameText.setText("User Name");
        userEmailText.setText("user@example.com");
        userRoleText.setText("Student");
        memberSinceText.setText("--");
        userIdText.setText("--");
        accountStatusText.setText("Not logged in");
        totalSessionsText.setText("0");
        learningHoursText.setText("0h");
        successRateText.setText("0%");
        currentStreakText.setText("0 days");
    }

    private void setDemoStatistics() {
        totalSessionsText.setText("12");
        learningHoursText.setText("24h");
        successRateText.setText("92%");
        currentStreakText.setText("5 days");
    }

    private void loadProfilePicture() {
        File imageFile = new File(requireContext().getFilesDir(), "profile_picture.jpg");
        if (imageFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            profileImage.setImageBitmap(bitmap);
        }
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
        try (FileOutputStream out = new FileOutputStream(new File(requireContext().getFilesDir(), "profile_picture.jpg"))) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
        } catch (IOException e) {
            Log.e(TAG, "Error saving profile picture", e);
        }
    }

    private void showEditProfileDialog() {
        Intent intent = new Intent(getActivity(), EditProfileActivity.class);
        startActivity(intent);
    }

    private void showChangePasswordDialog() {
        Intent intent = new Intent(getActivity(), EditProfileActivity.class);
        startActivity(intent);
    }

    private void openSettings() {
        Intent intent = new Intent(getActivity(), SettingsActivity.class);
        startActivity(intent);
    }

    private void logoutUser() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        File imageFile = new File(requireContext().getFilesDir(), "profile_picture.jpg");
        if (imageFile.exists()) imageFile.delete();

        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        startActivity(intent);
        requireActivity().finish();

        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
}

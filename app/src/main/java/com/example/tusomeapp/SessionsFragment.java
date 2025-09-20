package com.example.tusomeapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import java.util.Locale;

public class SessionsFragment extends Fragment {

    private TextView tvSessionCode, tvCallStatus, tvRecordingStatus;
    private ImageButton btnMic, btnCamera, btnScreenShare, btnRecord, btnEndCall;
    private FrameLayout videoContainer; // Changed from LinearLayout to FrameLayout
    private LinearLayout placeholderLayout, callStatusBar;

    private String currentSessionCode = "";
    private boolean isMicOn = true;
    private boolean isCameraOn = true; 
    private boolean isScreenSharing = false;
    private boolean isRecording = false;
    private boolean isCallActive = false;
    private SharedPreferences sharedPreferences;
    private CountDownTimer callTimer;
    private long callDuration = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sessions, container, false);

        // Initialize views
        tvSessionCode = view.findViewById(R.id.tvSessionCode);
        tvCallStatus = view.findViewById(R.id.tvCallStatus);
        tvRecordingStatus = view.findViewById(R.id.tvRecordingStatus);

        btnMic = view.findViewById(R.id.btnMic);
        btnCamera = view.findViewById(R.id.btnCamera);
        btnScreenShare = view.findViewById(R.id.btnScreenShare);
        btnRecord = view.findViewById(R.id.btnRecord);
        btnEndCall = view.findViewById(R.id.btnEndCall);

        videoContainer = view.findViewById(R.id.videoContainer); // This is a FrameLayout
        placeholderLayout = view.findViewById(R.id.placeholderLayout);
        callStatusBar = view.findViewById(R.id.callStatusBar);

        sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);

        // Set up button listeners
        btnMic.setOnClickListener(v -> toggleMicrophone());
        btnCamera.setOnClickListener(v -> toggleCamera());
        btnScreenShare.setOnClickListener(v -> toggleScreenShare());
        btnRecord.setOnClickListener(v -> toggleRecording());
        btnEndCall.setOnClickListener(v -> endCall());

        // Check for active session
        checkActiveSession();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkActiveSession();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (callTimer != null) {
            callTimer.cancel();
        }
    }

    private void checkActiveSession() {
        try {
            String sessionCode = sharedPreferences.getString("session_code", "");
            boolean isConnected = sharedPreferences.getBoolean("is_connected", false);

            if (!sessionCode.isEmpty() && isConnected) {
                currentSessionCode = sessionCode;
                tvSessionCode.setText("Session Code: " + sessionCode);
                // Auto-start call when session is active
                startCall();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error loading session", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCall() {
        try {
            isCallActive = true;

            // Update UI for active call
            placeholderLayout.setVisibility(View.GONE);
            callStatusBar.setVisibility(View.VISIBLE);

            // Start call timer
            startCallTimer();

            Toast.makeText(getContext(), "Call started", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error starting call", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleMicrophone() {
        isMicOn = !isMicOn;
        if (isMicOn) {
            btnMic.setImageResource(R.drawable.ic_mic_on);
            Toast.makeText(getContext(), "Microphone on", Toast.LENGTH_SHORT).show();
        } else {
            btnMic.setImageResource(R.drawable.ic_mic_off);
            Toast.makeText(getContext(), "Microphone off", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleCamera() {
        isCameraOn = !isCameraOn;
        if (isCameraOn) {
            btnCamera.setImageResource(R.drawable.ic_camera_on);
            Toast.makeText(getContext(), "Camera on", Toast.LENGTH_SHORT).show();
        } else {
            btnCamera.setImageResource(R.drawable.ic_camera_off);
            Toast.makeText(getContext(), "Camera off", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleScreenShare() {
        isScreenSharing = !isScreenSharing;
        if (isScreenSharing) {
            btnScreenShare.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue, requireContext().getTheme())));
            Toast.makeText(getContext(), "Screen sharing started", Toast.LENGTH_SHORT).show();
        } else {
            btnScreenShare.setImageTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.white, requireContext().getTheme())));
            Toast.makeText(getContext(), "Screen sharing stopped", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleRecording() {
        isRecording = !isRecording;
        if (isRecording) {
            btnRecord.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.red, requireContext().getTheme())));
            tvRecordingStatus.setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), "Recording started", Toast.LENGTH_SHORT).show();
        } else {
            btnRecord.setImageTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.white, requireContext().getTheme())));
            tvRecordingStatus.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Recording stopped", Toast.LENGTH_SHORT).show();
        }
    }

    private void endCall() {
        try {
            isCallActive = false;

            // Stop call timer
            if (callTimer != null) {
                callTimer.cancel();
            }

            // Reset UI
            placeholderLayout.setVisibility(View.VISIBLE);
            callStatusBar.setVisibility(View.GONE);

            // Reset buttons
            resetControlButtons();

            // Clear session data
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("session_code");
            editor.remove("is_connected");
            editor.apply();

            Toast.makeText(getContext(), "Call ended", Toast.LENGTH_SHORT).show();

            // Add to session history
            addToSessionHistory(currentSessionCode);
            currentSessionCode = "";
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error ending call", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCallTimer() {
        callTimer = new CountDownTimer(Long.MAX_VALUE, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                callDuration += 1000;
                updateCallTimer();
            }

            @Override
            public void onFinish() {
            }
        }.start();
    }

    private void updateCallTimer() {
        try {
            long seconds = callDuration / 1000;
            long minutes = seconds / 60;
            seconds = seconds % 60;

            String time = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
            tvCallStatus.setText(time);
        } catch (Exception e) {
            // Timer update error
        }
    }

    private void resetControlButtons() {
        isMicOn = true;
        isCameraOn = true;
        isScreenSharing = false;
        isRecording = false;

        btnMic.setImageResource(R.drawable.ic_mic_on);
        btnCamera.setImageResource(R.drawable.ic_camera_on);
        btnScreenShare.setImageTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.white, requireContext().getTheme())));
        btnRecord.setImageTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.white, requireContext().getTheme())));
    }

    private void addToSessionHistory(String sessionCode) {
        Toast.makeText(getContext(), "Session " + sessionCode + " added to history", Toast.LENGTH_SHORT).show();
    }

    public void setActiveSession(String sessionCode, boolean isHost) {
        try {
            this.currentSessionCode = sessionCode;
            tvSessionCode.setText("Session Code: " + sessionCode);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("session_code", sessionCode);
            editor.putBoolean("is_connected", true);
            editor.apply();

            // Auto-start the call
            startCall();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error setting active session", Toast.LENGTH_SHORT).show();
        }
    }
}
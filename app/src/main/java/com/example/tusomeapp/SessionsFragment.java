package com.example.tusomeapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SessionsFragment extends Fragment {

    private CardView cardActiveSession;
    private TextView tvSessionCode, tvConnectionStatus, tvNoActiveSession, tvNoHistory;
    private Button btnStartVideoCall, btnStartAudioCall, btnScreenShare, btnRecordSession, btnEndSession;
    private RecyclerView rvSessionHistory;

    private String currentSessionCode = "";
    private boolean isConnected = false;
    private boolean isCallActive = false;
    private boolean isRecording = false;
    private boolean isScreenSharing = false; // Added missing variable
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sessions, container, false);

        // Initialize views - FIXED ID NAMES to match your XML
        cardActiveSession = view.findViewById(R.id.cardActiveSession);
        tvSessionCode = view.findViewById(R.id.tvSessionCode);
        tvConnectionStatus = view.findViewById(R.id.tvConnectionStatus);
        tvNoActiveSession = view.findViewById(R.id.tvNoActiveSession);
        tvNoHistory = view.findViewById(R.id.tvNoHistory);
        btnStartVideoCall = view.findViewById(R.id.btnStartVideoCall);
        btnStartAudioCall = view.findViewById(R.id.btnStartAudioCall);
        btnScreenShare = view.findViewById(R.id.btnScreenShare);
        btnRecordSession = view.findViewById(R.id.btnRecordSession);
        btnEndSession = view.findViewById(R.id.btnEndSession);
        rvSessionHistory = view.findViewById(R.id.rvSessionHistory);

        sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);

        // Set up button listeners
        btnStartVideoCall.setOnClickListener(v -> startVideoCall());
        btnStartAudioCall.setOnClickListener(v -> startAudioCall());
        btnScreenShare.setOnClickListener(v -> toggleScreenShare());
        btnRecordSession.setOnClickListener(v -> toggleRecording());
        btnEndSession.setOnClickListener(v -> endSession());

        // Setup session history recycler view
        setupSessionHistory();

        // Check for active session
        checkActiveSession();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkActiveSession();
    }

    private void checkActiveSession() {
        String sessionCode = sharedPreferences.getString("session_code", "");
        boolean isConnected = sharedPreferences.getBoolean("is_connected", false);

        if (!sessionCode.isEmpty() && isConnected) {
            currentSessionCode = sessionCode;
            showActiveSession(sessionCode);
        } else {
            showNoActiveSession();
        }
    }

    private void showActiveSession(String sessionCode) {
        cardActiveSession.setVisibility(View.VISIBLE);
        tvNoActiveSession.setVisibility(View.GONE);

        tvSessionCode.setText("Session Code: " + sessionCode);
        tvConnectionStatus.setText("Status: Connected");
        tvConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark, requireContext().getTheme()));

        // Show call buttons
        btnStartVideoCall.setVisibility(View.VISIBLE);
        btnStartAudioCall.setVisibility(View.VISIBLE);
        btnScreenShare.setVisibility(View.VISIBLE);
        btnRecordSession.setVisibility(View.VISIBLE);
        btnEndSession.setVisibility(View.VISIBLE);
    }

    private void showNoActiveSession() {
        cardActiveSession.setVisibility(View.GONE);
        tvNoActiveSession.setVisibility(View.VISIBLE);
    }

    private void startVideoCall() {
        Toast.makeText(getContext(), "Starting video call...", Toast.LENGTH_SHORT).show();
        isCallActive = true;
        updateUIForActiveCall();
    }

    private void startAudioCall() {
        Toast.makeText(getContext(), "Starting audio call...", Toast.LENGTH_SHORT).show();
        isCallActive = true;
        updateUIForActiveCall();
    }

    private void toggleScreenShare() {
        isScreenSharing = !isScreenSharing;
        if (isScreenSharing) {
            Toast.makeText(getContext(), "Screen sharing started", Toast.LENGTH_SHORT).show();
            btnScreenShare.setText("Stop Sharing");
            btnScreenShare.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark, requireContext().getTheme())));
        } else {
            Toast.makeText(getContext(), "Screen sharing stopped", Toast.LENGTH_SHORT).show();
            btnScreenShare.setText("Share Screen");
            btnScreenShare.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_green_dark, requireContext().getTheme())));
        }
    }

    private void toggleRecording() {
        isRecording = !isRecording;
        if (isRecording) {
            Toast.makeText(getContext(), "Recording started", Toast.LENGTH_SHORT).show();
            btnRecordSession.setText("Stop Recording");
            btnRecordSession.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark, requireContext().getTheme())));
        } else {
            Toast.makeText(getContext(), "Recording stopped", Toast.LENGTH_SHORT).show();
            btnRecordSession.setText("Record");
            btnRecordSession.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark, requireContext().getTheme())));
        }
    }

    private void endSession() {
        // Clear session data
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("session_code");
        editor.remove("is_connected");
        editor.apply();

        // Reset UI
        showNoActiveSession();
        isCallActive = false;
        isScreenSharing = false;
        isRecording = false;

        Toast.makeText(getContext(), "Session ended", Toast.LENGTH_SHORT).show();

        // Add to session history
        addToSessionHistory(currentSessionCode);
        currentSessionCode = "";
    }

    private void updateUIForActiveCall() {
        // Update UI when call is active
        btnStartVideoCall.setVisibility(View.GONE);
        btnStartAudioCall.setVisibility(View.GONE);
        // You can add more UI updates here for active call state
    }

    private void setupSessionHistory() {
        // For now, we'll just hide the recyclerview and show "no history" message
        // You can implement this later with actual data
        rvSessionHistory.setVisibility(View.GONE);
        tvNoHistory.setVisibility(View.VISIBLE);
    }

    private void addToSessionHistory(String sessionCode) {
        // Add session to history (you can implement this with SharedPreferences or database)
        // For now, we'll just show a toast
        Toast.makeText(getContext(), "Session " + sessionCode + " added to history", Toast.LENGTH_SHORT).show();
    }

    // Call this method from HomeFragment when a session is created or joined
    public void setActiveSession(String sessionCode, boolean isHost) {
        this.currentSessionCode = sessionCode;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("session_code", sessionCode);
        editor.putBoolean("is_connected", true);
        editor.apply();

        showActiveSession(sessionCode);
    }
}
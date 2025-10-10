package com.example.tusomeapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import java.util.Random;

public class HomeFragment extends Fragment {

    private TextView tvWelcome, tvSessionCode, tvConnectionStatus;
    private Button btnCreateSession, btnJoinSession, btnStartCall;
    private EditText etSessionCode;
    private CardView cardSessionStatus;
    private String currentSessionCode = "";
    private boolean isConnected = false;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        tvWelcome = view.findViewById(R.id.tvWelcome);
        btnCreateSession = view.findViewById(R.id.btnCreateSession);
        btnJoinSession = view.findViewById(R.id.btnJoinSession);
        etSessionCode = view.findViewById(R.id.etSessionCode);
        cardSessionStatus = view.findViewById(R.id.cardSessionStatus);
        tvSessionCode = view.findViewById(R.id.tvSessionCode);
        tvConnectionStatus = view.findViewById(R.id.tvConnectionStatus);
        btnStartCall = view.findViewById(R.id.btnStartCall);

        // Load username from SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "User");
        tvWelcome.setText("Welcome, " + username + " 👋");

        // Set click listeners
        btnCreateSession.setOnClickListener(v -> createSession());
        btnJoinSession.setOnClickListener(v -> joinSession());
        btnStartCall.setOnClickListener(v -> startCall());

        return view;
    }

    private void createSession() {
        // Generate random 4-digit code
        Random random = new Random();
        currentSessionCode = String.format("%04d", random.nextInt(10000));

        // Fade in session card
        fadeInView(cardSessionStatus);

        tvSessionCode.setText("Session Code: " + currentSessionCode);
        tvConnectionStatus.setText("Status: Waiting for partner...");
        tvConnectionStatus.setTextColor(getResources().getColor(R.color.orange));

        // Save in preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("session_code", currentSessionCode);
        editor.putBoolean("is_connected", false);
        editor.apply();

        // Simulate connection delay (3 seconds)
        new Handler().postDelayed(() -> simulateConnection(true), 3000);
    }

    private void joinSession() {
        String inputCode = etSessionCode.getText().toString().trim();

        if (inputCode.length() != 4) {
            Toast.makeText(getContext(), "Please enter a valid 4-digit code", Toast.LENGTH_SHORT).show();
            return;
        }

        currentSessionCode = inputCode;

        // Save entered code
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("session_code", currentSessionCode);
        editor.putBoolean("is_connected", false);
        editor.apply();

        fadeInView(cardSessionStatus);
        simulateConnection(false);
    }

    private void simulateConnection(boolean isHost) {
        isConnected = true;
        String partnerName = isHost ? "Partner" : "Host";

        // Update UI
        cardSessionStatus.setVisibility(View.VISIBLE);
        tvSessionCode.setText("Session Code: " + currentSessionCode);
        tvConnectionStatus.setText("Status: Connected to " + partnerName);
        tvConnectionStatus.setTextColor(getResources().getColor(R.color.green));
        btnStartCall.setVisibility(View.VISIBLE);

        // Hide other controls
        btnCreateSession.setVisibility(View.GONE);
        btnJoinSession.setVisibility(View.GONE);
        etSessionCode.setVisibility(View.GONE);

        // Update SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("is_connected", true);
        editor.apply();

        // Notify other fragment
        notifySessionsFragment(currentSessionCode, isHost);

        Toast.makeText(getContext(), "Connected successfully 🎉", Toast.LENGTH_SHORT).show();
    }

    private void startCall() {
        Toast.makeText(getContext(),
                "Starting call with session: " + currentSessionCode,
                Toast.LENGTH_SHORT).show();

        // Future implementation: Launch call activity here
    }

    private void notifySessionsFragment(String sessionCode, boolean isHost) {
        try {
            FragmentManager fragmentManager = getParentFragmentManager();
            Fragment sessionsFragment =
                    fragmentManager.findFragmentByTag("android:switcher:" + R.id.viewPager + ":1");

            if (sessionsFragment instanceof SessionsFragment) {
                ((SessionsFragment) sessionsFragment).setActiveSession(sessionCode, isHost);
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Couldn't update sessions tab", Toast.LENGTH_SHORT).show();
        }
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

            fadeInView(cardSessionStatus);

            tvSessionCode.setText("Session Code: " + sessionCode);
            tvConnectionStatus.setText("Status: Connected");
            tvConnectionStatus.setTextColor(getResources().getColor(R.color.green));
            btnStartCall.setVisibility(View.VISIBLE);

            btnCreateSession.setVisibility(View.GONE);
            btnJoinSession.setVisibility(View.GONE);
            etSessionCode.setVisibility(View.GONE);
        }
    }

    private void fadeInView(View view) {
        view.setVisibility(View.VISIBLE);
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(500);
        view.startAnimation(fadeIn);
    }
}

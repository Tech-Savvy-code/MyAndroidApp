package com.example.tusomeapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        // Get username from SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "User");
        tvWelcome.setText("Welcome " + username + "!");

        // Set up button click listeners
        btnCreateSession.setOnClickListener(v -> createSession());
        btnJoinSession.setOnClickListener(v -> joinSession());
        btnStartCall.setOnClickListener(v -> startCall());

        return view;
    }

    private void createSession() {
        // Generate a random 4-digit session code
        Random random = new Random();
        currentSessionCode = String.format("%04d", random.nextInt(10000));

        // Display the session status card
        cardSessionStatus.setVisibility(View.VISIBLE);
        tvSessionCode.setText("Session Code: " + currentSessionCode);
        tvConnectionStatus.setText("Status: Waiting for partner...");
        tvConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));

        // Save session code to preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("session_code", currentSessionCode);
        editor.putBoolean("is_connected", false); // Not connected yet
        editor.apply();

        // Simulate connection after a delay (for demo purposes)
        new Handler().postDelayed(
                () -> simulateConnection(true),
                3000
        );
    }

    private void joinSession() {
        String inputCode = etSessionCode.getText().toString().trim();

        if (inputCode.length() != 4) {
            Toast.makeText(getContext(), "Please enter a valid 4-digit code", Toast.LENGTH_SHORT).show();
            return;
        }

        currentSessionCode = inputCode;

        // Save session code to preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("session_code", currentSessionCode);
        editor.putBoolean("is_connected", false); // Not connected yet
        editor.apply();

        simulateConnection(false);
    }

    private void simulateConnection(boolean isHost) {
        isConnected = true;
        String partnerName = isHost ? "Partner" : "Host";

        // Update UI
        cardSessionStatus.setVisibility(View.VISIBLE);
        tvSessionCode.setText("Session Code: " + currentSessionCode);
        tvConnectionStatus.setText("Status: Connected to " + partnerName);
        tvConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        btnStartCall.setVisibility(View.VISIBLE);

        // Hide create/join buttons
        btnCreateSession.setVisibility(View.GONE);
        btnJoinSession.setVisibility(View.GONE);
        etSessionCode.setVisibility(View.GONE);

        // Update shared preferences with connection status
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("is_connected", true);
        editor.apply();

        // Notify SessionsFragment about the active session
        notifySessionsFragment(currentSessionCode, isHost);

        Toast.makeText(getContext(), "Successfully connected!", Toast.LENGTH_SHORT).show();
    }

    private void startCall() {
        // For now, just show a toast. You can implement actual call functionality later.
        Toast.makeText(getContext(), "Starting call with session: " + currentSessionCode, Toast.LENGTH_SHORT).show();

        // Here you would typically launch your call activity
        // Intent intent = new Intent(getActivity(), CallActivity.class);
        // intent.putExtra("SESSION_CODE", currentSessionCode);
        // startActivity(intent);
    }

    // Method to notify SessionsFragment about the active session
    private void notifySessionsFragment(String sessionCode, boolean isHost) {
        try {
            // Get the FragmentManager
            FragmentManager fragmentManager = getParentFragmentManager();

            // Try to find the SessionsFragment
            Fragment sessionsFragment = fragmentManager.findFragmentByTag("android:switcher:" + R.id.viewPager + ":1");

            // If we found the SessionsFragment and it's an instance of our class
            if (sessionsFragment != null && sessionsFragment instanceof SessionsFragment) {
                ((SessionsFragment) sessionsFragment).setActiveSession(sessionCode, isHost);
            } else {
                // Alternative approach: use FragmentManager to find by ID
                sessionsFragment = fragmentManager.findFragmentById(R.id.viewPager);
                if (sessionsFragment != null && sessionsFragment instanceof SessionsFragment) {
                    ((SessionsFragment) sessionsFragment).setActiveSession(sessionCode, isHost);
                }
            }
        } catch (Exception e) {
            // Handle any exceptions (fragment might not be initialized yet)
            Toast.makeText(getContext(), "Could not update sessions tab", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check if we have an active session when fragment resumes
        checkActiveSession();
    }

    private void checkActiveSession() {
        String sessionCode = sharedPreferences.getString("session_code", "");
        boolean isConnected = sharedPreferences.getBoolean("is_connected", false);

        if (!sessionCode.isEmpty() && isConnected) {
            currentSessionCode = sessionCode;

            // Update UI to show active session
            cardSessionStatus.setVisibility(View.VISIBLE);
            tvSessionCode.setText("Session Code: " + sessionCode);
            tvConnectionStatus.setText("Status: Connected");
            tvConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            btnStartCall.setVisibility(View.VISIBLE);

            // Hide create/join buttons
            btnCreateSession.setVisibility(View.GONE);
            btnJoinSession.setVisibility(View.GONE);
            etSessionCode.setVisibility(View.GONE);
        }
    }
}
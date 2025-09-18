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
}
package com.example.tusomeapp;

import android.annotation.SuppressLint;
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
import androidx.fragment.app.FragmentTransaction;

import java.util.Random;

public class HomeFragment extends Fragment {

    private TextView tvWelcome, tvSessionCode, tvConnectionStatus;
    private Button btnCreateSession, btnJoinSession, btnStartCall, btnBackToDashboard;
    private EditText etSessionCode;
    private CardView cardSessionStatus;
    private String currentSessionCode = "";
    private boolean isConnected = false;
    private SharedPreferences sharedPreferences;

    @SuppressLint("MissingInflatedId")
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
        btnBackToDashboard = view.findViewById(R.id.btnBackToDashboard);

        sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "User");
        tvWelcome.setText("Welcome " + username + "!");

        btnCreateSession.setOnClickListener(v -> createSession());
        btnJoinSession.setOnClickListener(v -> joinSession());
        btnStartCall.setOnClickListener(v -> startCall());
        btnBackToDashboard.setOnClickListener(v -> resetToDashboard());

        return view;
    }

    private void createSession() {
        Random random = new Random();
        currentSessionCode = String.format("%04d", random.nextInt(10000));

        cardSessionStatus.setVisibility(View.VISIBLE);
        tvSessionCode.setText("Session Code: " + currentSessionCode);
        tvConnectionStatus.setText("Status: Waiting for partner...");
        tvConnectionStatus.setTextColor(getResources().getColor(R.color.orange));

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("session_code", currentSessionCode);
        editor.putBoolean("is_connected", false);
        editor.apply();

        new Handler().postDelayed(() -> simulateConnection(true), 3000);
    }

    private void joinSession() {
        String inputCode = etSessionCode.getText().toString().trim();

        if (inputCode.length() != 4) {
            Toast.makeText(getContext(), "Please enter a valid 4-digit code", Toast.LENGTH_SHORT).show();
            return;
        }

        currentSessionCode = inputCode;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("session_code", currentSessionCode);
        editor.putBoolean("is_connected", false);
        editor.apply();

        simulateConnection(false);
    }

    private void simulateConnection(boolean isHost) {
        isConnected = true;
        String partnerName = isHost ? "Partner" : "Host";

        cardSessionStatus.setVisibility(View.VISIBLE);
        tvSessionCode.setText("Session Code: " + currentSessionCode);
        tvConnectionStatus.setText("Status: Connected to " + partnerName);
        tvConnectionStatus.setTextColor(getResources().getColor(R.color.green));
        btnStartCall.setVisibility(View.VISIBLE);
        btnBackToDashboard.setVisibility(View.VISIBLE);

        btnCreateSession.setVisibility(View.GONE);
        btnJoinSession.setVisibility(View.GONE);
        etSessionCode.setVisibility(View.GONE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("is_connected", true);
        editor.apply();

        notifySessionsFragment(currentSessionCode, isHost);

        Toast.makeText(getContext(), "Successfully connected!", Toast.LENGTH_SHORT).show();
    }

    private void resetToDashboard() {
        cardSessionStatus.setVisibility(View.GONE);
        btnStartCall.setVisibility(View.GONE);
        btnBackToDashboard.setVisibility(View.GONE);

        btnCreateSession.setVisibility(View.VISIBLE);
        btnJoinSession.setVisibility(View.VISIBLE);
        etSessionCode.setVisibility(View.VISIBLE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("session_code");
        editor.putBoolean("is_connected", false);
        editor.apply();

        Toast.makeText(getContext(), "Returned to Dashboard", Toast.LENGTH_SHORT).show();
    }

    // ✅ Updated method to properly open SessionsFragment
    private void startCall() {
        if (currentSessionCode.isEmpty()) {
            Toast.makeText(getContext(), "No active session found.", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("in_call", true);
        editor.apply();

        // ✅ Open SessionsFragment directly (not in ViewPager)
        SessionsFragment sessionsFragment = new SessionsFragment();
        Bundle args = new Bundle();
        args.putString("session_code", currentSessionCode);
        args.putBoolean("is_host", true);
        sessionsFragment.setArguments(args);

        FragmentTransaction transaction = requireActivity()
                .getSupportFragmentManager()
                .beginTransaction();

        transaction.setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
        );
        transaction.replace(R.id.fragment_container, sessionsFragment);
        transaction.addToBackStack(null);
        transaction.commit();

        Toast.makeText(getContext(), "Opening session view...", Toast.LENGTH_SHORT).show();
    }

    private void notifySessionsFragment(String sessionCode, boolean isHost) {
        try {
            FragmentManager fragmentManager = getParentFragmentManager();
            Fragment sessionsFragment = fragmentManager.findFragmentByTag("SessionsFragment");

            if (sessionsFragment != null && sessionsFragment instanceof SessionsFragment) {
                ((SessionsFragment) sessionsFragment).setActiveSession(sessionCode, isHost);
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Could not update sessions tab", Toast.LENGTH_SHORT).show();
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
            cardSessionStatus.setVisibility(View.VISIBLE);
            tvSessionCode.setText("Session Code: " + sessionCode);
            tvConnectionStatus.setText("Status: Connected");
            tvConnectionStatus.setTextColor(getResources().getColor(R.color.green));
            btnStartCall.setVisibility(View.VISIBLE);
            btnBackToDashboard.setVisibility(View.VISIBLE);

            btnCreateSession.setVisibility(View.GONE);
            btnJoinSession.setVisibility(View.GONE);
            etSessionCode.setVisibility(View.GONE);
        }
    }
}

package com.example.tusomeapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public abstract class BaseFragment extends Fragment {

    private LinearLayout noInternetLayout;

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (view != null) {
            noInternetLayout = view.findViewById(R.id.noInternetLayout);
            checkInternetAndUpdateUI();
        }
    }

    private void checkInternetAndUpdateUI() {
        boolean isConnected = NetworkUtils.isNetworkAvailable(requireContext());

        if (noInternetLayout != null) {
            noInternetLayout.setVisibility(isConnected ? View.GONE : View.VISIBLE);

            if (!isConnected) {
                Toast.makeText(requireContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();

                Button retryButton = noInternetLayout.findViewById(R.id.btnRetry);
                retryButton.setOnClickListener(v -> checkInternetAndUpdateUI());
            }
        }
    }
}

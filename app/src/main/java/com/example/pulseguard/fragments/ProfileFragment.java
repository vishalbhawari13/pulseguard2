package com.example.pulseguard.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.pulseguard.R;
import com.example.pulseguard.activities.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {
    private TextView tvUsername, tvEmail;
    private Button btnLogout;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize UI components
        tvUsername = view.findViewById(R.id.tv_username);
        tvEmail = view.findViewById(R.id.tv_email);
        btnLogout = view.findViewById(R.id.btn_logout);

        // Get user details from Firebase Auth
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            tvUsername.setText(user.getDisplayName());
            tvEmail.setText(user.getEmail());
        } else {
            tvUsername.setText("Guest");
            tvEmail.setText("No Email Available");
        }

        // Logout button click listener
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            // Clear shared preferences (if storing user data)
            SharedPreferences preferences = requireActivity().getSharedPreferences("UserPrefs", 0);
            preferences.edit().clear().apply();

            // Redirect to LoginActivity
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }
}

package com.example.pulseguard.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pulseguard.R;
import com.example.pulseguard.activities.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private ImageView profileImage;
    private TextView tvUsername, tvEmail;
    private Button btnEditProfile, btnLogout;

    private FirebaseAuth mAuth;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        profileImage = rootView.findViewById(R.id.profile_image);
        tvUsername = rootView.findViewById(R.id.tv_username);
        tvEmail = rootView.findViewById(R.id.tv_email);
        btnEditProfile = rootView.findViewById(R.id.btn_edit_profile);
        btnLogout = rootView.findViewById(R.id.btn_logout);

        // Get current user details
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            tvUsername.setText(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Username");
            tvEmail.setText(currentUser.getEmail());
            // You can add profile image loading here using libraries like Glide or Picasso
            // For now, setting a default profile image
            profileImage.setImageResource(R.drawable.ic_profile);
        }

        // Set button listeners
        btnEditProfile.setOnClickListener(v -> {
            // Handle edit profile (you can navigate to another activity or fragment to edit profile)
            // Example: startActivity(new Intent(getContext(), EditProfileActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            // Handle logout
            mAuth.signOut();
            // Redirect to Login screen after logout
            Intent loginIntent = new Intent(getContext(), LoginActivity.class);
            startActivity(loginIntent);
            getActivity().finish();
        });

        return rootView;
    }
}

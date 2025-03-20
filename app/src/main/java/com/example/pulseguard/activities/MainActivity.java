package com.example.pulseguard.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pulseguard.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;

    private TextView welcomeText;
    private Button signInButton, signOutButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Ensure this matches the Firebase project
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize UI Components
        welcomeText = findViewById(R.id.welcome_text);
        signInButton = findViewById(R.id.sign_in_button);
        signOutButton = findViewById(R.id.sign_out_button);
        progressBar = findViewById(R.id.progress_bar);

        // Set Button Click Listeners
        signInButton.setOnClickListener(v -> signIn());
        signOutButton.setOnClickListener(v -> signOut());

        // Check if a user is already signed in
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        updateUI(currentUser);
    }

    /**
     * ActivityResultLauncher to handle Google Sign-In result
     */
    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account != null) {
                            firebaseAuthWithGoogle(account);
                        }
                    } catch (ApiException e) {
                        Log.e(TAG, "Google Sign-In failed: " + e.getMessage());
                        Toast.makeText(MainActivity.this, "Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                } else {
                    Log.w(TAG, "Google Sign-In canceled or failed.");
                    Toast.makeText(MainActivity.this, "Sign-In canceled", Toast.LENGTH_SHORT).show();
                }
            });

    /**
     * Initiates Google Sign-In
     */
    private void signIn() {
        progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    /**
     * Authenticate with Firebase using Google Account credentials
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "Authenticating with Firebase...");

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        Log.d(TAG, "Sign-in successful: " + (user != null ? user.getDisplayName() : "Unknown User"));
                        updateUI(user);
                    } else {
                        Log.e(TAG, "Firebase Authentication Failed!", task.getException());
                        Toast.makeText(MainActivity.this, "Authentication Failed!", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    /**
     * Logs out the user from Firebase and Google
     */
    private void signOut() {
        progressBar.setVisibility(View.VISIBLE);
        firebaseAuth.signOut();
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            progressBar.setVisibility(View.GONE);
            updateUI(null);
        });
    }

    /**
     * Updates the UI based on Firebase authentication status
     */
    private void updateUI(FirebaseUser user) {
        progressBar.setVisibility(View.GONE);
        if (user != null) {
            String welcomeMessage = "Welcome to PulseGuard, " + user.getDisplayName() + "!";
            welcomeText.setText(welcomeMessage);
            welcomeText.setVisibility(View.VISIBLE);
            signInButton.setVisibility(View.GONE);
            signOutButton.setVisibility(View.VISIBLE);
        } else {
            welcomeText.setText("Please Sign In");
            welcomeText.setVisibility(View.VISIBLE);
            signInButton.setVisibility(View.VISIBLE);
            signOutButton.setVisibility(View.GONE);
        }
    }
}

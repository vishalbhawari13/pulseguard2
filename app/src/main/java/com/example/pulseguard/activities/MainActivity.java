package com.example.pulseguard.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_GOOGLE_FIT = 1001;

    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInAccount googleSignInAccount;

    private TextView welcomeText;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize UI Components
        welcomeText = findViewById(R.id.welcome_text);
        progressBar = findViewById(R.id.progress_bar);
        findViewById(R.id.sign_in_button).setOnClickListener(v -> signIn());

        // Check if a user is already signed in
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        updateUI(currentUser);

        // Set up Google Fit permissions
        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                .build();
        googleSignInAccount = GoogleSignIn.getAccountForExtension(this, fitnessOptions);

        // Check if the user has already granted permissions
        if (!GoogleSignIn.hasPermissions(googleSignInAccount, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    REQUEST_CODE_GOOGLE_FIT,
                    googleSignInAccount,
                    fitnessOptions);
        } else {
            accessGoogleFitData(googleSignInAccount);
        }
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
                        navigateToDashboard();
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
     * Updates the UI based on Firebase authentication status.
     */
    private void updateUI(FirebaseUser user) {
        progressBar.setVisibility(View.GONE);
        if (user != null) {
            navigateToDashboard();
        } else {
            welcomeText.setText("Please Sign In");
            welcomeText.setVisibility(View.VISIBLE);
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
        }
    }

    /**
     * Navigates to DashboardActivity if user is authenticated
     */
    private void navigateToDashboard() {
        startActivity(new Intent(MainActivity.this, DashboardActivity.class));
        finish();
    }

    // Handle the result of Google Sign-In permission request for Google Fit
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GOOGLE_FIT) {
            if (resultCode == RESULT_OK) {
                accessGoogleFitData(googleSignInAccount);
            }
        }
    }

    // Access Google Fit data
    public void accessGoogleFitData(GoogleSignInAccount account) {
        Fitness.getHistoryClient(this, account)
                .readData(new com.google.android.gms.fitness.request.DataReadRequest.Builder()
                        .read(DataType.TYPE_HEART_RATE_BPM)
                        .setTimeRange(1, System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                        .build())
                .addOnSuccessListener(dataReadResponse -> {
                    for (DataSet dataSet : dataReadResponse.getDataSets()) {
                        for (DataPoint dataPoint : dataSet.getDataPoints()) {
                            // Extract heart rate data
                            float heartRate = dataPoint.getValue(Field.FIELD_INTENSITY).asFloat();
                            Log.d(TAG, "Heart Rate: " + heartRate);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error reading heart rate data", e));
    }
}

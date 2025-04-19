package com.example.pulseguard.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pulseguard.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final int REQUEST_OAUTH_REQUEST_CODE = 1;

    private EditText emailEditText, passwordEditText;
    private Button loginButton, signupButton;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Bind UI elements
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);
        progressBar = findViewById(R.id.progressBar);

        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToDashboard();
        }

        // Configure Google Fit Sign-In
        configureGoogleFit();

        // Login Button Click
        loginButton.setOnClickListener(v -> loginUser());

        // Signup Button Click
        signupButton.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
    }

    private void configureGoogleFit() {
        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                .build();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null || !GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions
            );
        }
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required!");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required!");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        navigateToDashboard();
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed! " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish(); // Prevent returning to login
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                fetchHealthData(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Google Fit permission denied: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchHealthData(GoogleSignInAccount account) {
        if (account != null) {
            Toast.makeText(this, "Google Fit connected!", Toast.LENGTH_SHORT).show();
            // TODO: Implement data retrieval from Google Fit API
            // You can now start requesting health data such as step count, heart rate, and calories burned
        }
    }
}

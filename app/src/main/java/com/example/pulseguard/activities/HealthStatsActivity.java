package com.example.pulseguard.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.pulseguard.R;

public class HealthStatsActivity extends AppCompatActivity {

    private TextView heartRateText, sleepText, spo2Text, stepsText;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_stats);

        // Initialize UI elements
        heartRateText = findViewById(R.id.tv_heart_rate);
        sleepText = findViewById(R.id.tv_sleep);
        spo2Text = findViewById(R.id.tv_spo2);
        stepsText = findViewById(R.id.tv_steps);
        progressBar = findViewById(R.id.progressBar);

        // Fetch Data from API (Replace with actual API call in the future)
        fetchHealthData();
    }

    private void fetchHealthData() {
        // Show progress while fetching data
        progressBar.setVisibility(View.VISIBLE);

        // Simulating API response with a delay
        new android.os.Handler().postDelayed(() -> {
            // Update UI with simulated health data
            updateHealthStats("78 BPM", "7 hrs 30 min", "97%", "5,630");
        }, 1500); // Simulate API delay of 1.5 seconds
    }

    private void updateHealthStats(String heartRate, String sleep, String spo2, String steps) {
        if (heartRateText != null) heartRateText.setText("Heart Rate: " + heartRate);
        if (sleepText != null) sleepText.setText("Sleep: " + sleep);
        if (spo2Text != null) spo2Text.setText("SpO2: " + spo2);
        if (stepsText != null) stepsText.setText("Steps: " + steps);

        // Hide progress bar after data is loaded
        progressBar.setVisibility(View.GONE);
    }
}

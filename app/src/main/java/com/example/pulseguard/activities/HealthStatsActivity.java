package com.example.pulseguard.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import android.widget.TextView;

import com.example.pulseguard.R;
import com.example.pulseguard.utils.GoogleFitHelper;
import com.example.pulseguard.viewmodel.HealthStatsViewModel;

public class HealthStatsActivity extends AppCompatActivity {
    private HealthStatsViewModel healthStatsViewModel;
    private GoogleFitHelper googleFitHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_stats);

        // Initialize ViewModel
        healthStatsViewModel = new ViewModelProvider(this).get(HealthStatsViewModel.class);

        // UI Elements
        TextView sleepDurationText = findViewById(R.id.sleep_duration);
        TextView stepCountText = findViewById(R.id.step_count);
        TextView heartRateText = findViewById(R.id.heart_rate);
        TextView caloriesBurnedText = findViewById(R.id.calories_burned);

        // Observe LiveData and update UI automatically
        healthStatsViewModel.getSleepDuration().observe(this, sleepDurationText::setText);
        healthStatsViewModel.getStepCount().observe(this, stepCountText::setText);
        healthStatsViewModel.getHeartRate().observe(this, heartRateText::setText);
        healthStatsViewModel.getCaloriesBurned().observe(this, caloriesBurnedText::setText);

        // Initialize Google Fit Helper
        googleFitHelper = new GoogleFitHelper(this);
        googleFitHelper.requestGoogleFitPermissions();

        // Fetch real-time data from Google Fit
        googleFitHelper.setHealthDataListener((label, value) -> runOnUiThread(() -> {
            switch (label) {
                case "Sleep":
                    healthStatsViewModel.updateSleepDuration((int) value / 60, (int) value % 60);
                    break;
                case "Step Count":
                    healthStatsViewModel.updateStepCount((int) value);
                    break;
                case "Heart Rate":
                    healthStatsViewModel.updateHeartRate((int) value);
                    break;
                case "Calories Burned":
                    healthStatsViewModel.updateCaloriesBurned((int) value);
                    break;
            }
        }));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Fetch historical health data from Google Fit when activity is resumed
        googleFitHelper.fetchHistoricalHealthData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Removed cleanup if not necessary
    }

}

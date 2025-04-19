package com.example.pulseguard.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.pulseguard.R;
import com.example.pulseguard.utils.GoogleFitHelper;
import com.example.pulseguard.viewmodel.HealthStatsViewModel;

public class HealthStatsActivity extends AppCompatActivity {

    private HealthStatsViewModel healthStatsViewModel;
    private GoogleFitHelper googleFitHelper;

    private TextView sleepDurationText;
    private TextView stepCountText;
    private TextView heartRateText;
    private TextView caloriesBurnedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_stats);

        initViews();
        setupViewModel();
        setupGoogleFit();
    }

    private void initViews() {
        sleepDurationText = findViewById(R.id.sleep_duration);
        stepCountText = findViewById(R.id.step_count);
        heartRateText = findViewById(R.id.heart_rate);
        caloriesBurnedText = findViewById(R.id.calories_burned);
    }

    private void setupViewModel() {
        // Initialize ViewModel
        healthStatsViewModel = new ViewModelProvider(this).get(HealthStatsViewModel.class);

        // Observe changes in health data and update UI accordingly
        healthStatsViewModel.getSleepDuration().observe(this, duration -> {
            // Ensure that 'duration' is an integer representing minutes
            int durationInt = Integer.parseInt(duration);  // Convert String to int
            int hours = durationInt / 60;
            int minutes = durationInt % 60;

            sleepDurationText.setText("Sleep: " + hours + "h " + minutes + "m");
        });


        healthStatsViewModel.getStepCount().observe(this, steps -> {
            stepCountText.setText("Steps: " + steps + " steps");
        });

        healthStatsViewModel.getHeartRate().observe(this, bpm -> {
            heartRateText.setText("Heart Rate: " + bpm + " BPM");
        });

        healthStatsViewModel.getCaloriesBurned().observe(this, calories -> {
            caloriesBurnedText.setText("Calories Burned: " + calories + " kcal");
        });
    }

    private void setupGoogleFit() {
        googleFitHelper = new GoogleFitHelper(this);
        googleFitHelper.requestGoogleFitPermissions();

        // Set listener to handle received health data
        googleFitHelper.setHealthDataListener((label, value) -> runOnUiThread(() -> {
            // Update ViewModel with the latest health data
            switch (label) {
                case "Sleep":
                    int hours = (int) value / 60;
                    int minutes = (int) value % 60;
                    healthStatsViewModel.updateSleepDuration(hours, minutes);
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

                default:
                    break;
            }
        }));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh today's health data from Google Fit when activity resumes
        googleFitHelper.fetchTodayHealthData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Optional: clean up or pause background work here if needed
    }
}

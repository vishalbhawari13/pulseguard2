package com.example.pulseguard.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.pulseguard.R;
import com.example.pulseguard.utils.GoogleFitHelper;

public class HealthStatsActivity extends AppCompatActivity {
    private GoogleFitHelper googleFitHelper;
    private TextView stepCountText, caloriesBurnedText, heartRateText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_stats);

        // Initialize UI elements
        stepCountText = findViewById(R.id.step_count);
        caloriesBurnedText = findViewById(R.id.calories_burned);
        heartRateText = findViewById(R.id.heart_rate);

        // Initialize Google Fit Helper
        googleFitHelper = new GoogleFitHelper(this);
        googleFitHelper.requestGoogleFitPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        googleFitHelper.fetchHealthData();
    }
}

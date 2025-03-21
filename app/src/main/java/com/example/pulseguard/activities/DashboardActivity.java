package com.example.pulseguard.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pulseguard.R;

public class DashboardActivity extends AppCompatActivity {

    private Button btnStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize button
        btnStats = findViewById(R.id.btn_stats);

        // Set click listener to open HealthStatsActivity
        btnStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, HealthStatsActivity.class);
                startActivity(intent);
            }
        });
    }
}

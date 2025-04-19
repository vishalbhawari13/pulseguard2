package com.example.pulseguard.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class DeepLinkActivity extends AppCompatActivity {

    private static final String TAG = "DeepLinkActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the intent and URI
        Intent intent = getIntent();
        Uri data = intent.getData();

        if (data != null) {
            Log.d(TAG, "Deep link received: " + data.toString());

            // Example: Parse the path or query and take action
            String path = data.getPath(); // e.g., /dashboard or /profile
            String action = data.getQueryParameter("action"); // e.g., action=view

            if (path != null) {
                switch (path) {
                    case "/dashboard":
                        startActivity(new Intent(this, DashboardActivity.class));
                        break;
                    case "/health-stats":
                        startActivity(new Intent(this, HealthStatsActivity.class));
                        break;
                    default:
                        Log.w(TAG, "Unknown path: " + path);
                        break;
                }
            }

            if (action != null) {
                Log.d(TAG, "Action parameter: " + action);
                // Optionally handle actions via query parameters
            }
        } else {
            Log.w(TAG, "No deep link data found in the intent.");
        }

        // Finish this intermediate activity
        finish();
    }
}

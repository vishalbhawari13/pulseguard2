package com.example.pulseguard.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class DeepLinkActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Handle the deep link
        Intent intent = getIntent();
        Uri data = intent.getData();

        if (data != null) {
            // Process the deep link data (Example: open specific screen)
        }

        // Finish the activity once the deep link is processed
        finish();
    }
}

package com.example.pulseguard.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pulseguard.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class DashboardActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_GOOGLE_FIT = 1001;
    private static final String TAG = "DashboardActivity";

    private TextView textViewHeartRate, textViewStepCount, textViewCalories;
    private FitnessOptions fitnessOptions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize views
        textViewHeartRate = findViewById(R.id.textViewHeartRate);
        textViewStepCount = findViewById(R.id.textViewStepCount);
        textViewCalories = findViewById(R.id.textViewCalories);
        Button btnStats = findViewById(R.id.btn_stats);

        // Setup fitness options
        fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_HEART_RATE_SUMMARY, FitnessOptions.ACCESS_READ)
                .build();

        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(this, fitnessOptions);

        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    REQUEST_CODE_GOOGLE_FIT,
                    account,
                    fitnessOptions
            );
        } else {
            fetchAllFitnessData(account);
        }

        btnStats.setOnClickListener(v -> navigateToHealthStatsActivity());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GOOGLE_FIT) {
            GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(this, fitnessOptions);
            if (resultCode == RESULT_OK && account != null) {
                fetchAllFitnessData(account);
            } else {
                Toast.makeText(this, "Permission denied. Cannot fetch health data.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private long getStartOfTodayMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private void fetchAllFitnessData(GoogleSignInAccount account) {
        long startTime = getStartOfTodayMillis();
        long endTime = System.currentTimeMillis();

        fetchHeartRate(account, startTime, endTime);
        fetchStepCount(account, startTime, endTime);
        fetchCalories(account, startTime, endTime);
    }

    private void fetchHeartRate(GoogleSignInAccount account, long startTime, long endTime) {
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_HEART_RATE_BPM, DataType.AGGREGATE_HEART_RATE_SUMMARY)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(this, account)
                .readData(readRequest)
                .addOnSuccessListener(response -> {
                    float avgBpm = 0;
                    int count = 0;
                    for (Bucket bucket : response.getBuckets()) {
                        for (DataSet dataSet : bucket.getDataSets()) {
                            for (DataPoint dp : dataSet.getDataPoints()) {
                                if (dp.getValue(Field.FIELD_AVERAGE) != null) {
                                    avgBpm += dp.getValue(Field.FIELD_AVERAGE).asFloat();
                                    count++;
                                }
                            }
                        }
                    }
                    if (count > 0) {
                        avgBpm = avgBpm / count;
                        textViewHeartRate.setText("Heart Rate: " + String.format("%.1f", avgBpm) + " BPM");
                    } else {
                        textViewHeartRate.setText("Heart Rate: No Data");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Heart Rate fetch failed", e);
                    textViewHeartRate.setText("Heart Rate: Error");
                });
    }


    private void fetchStepCount(GoogleSignInAccount account, long startTime, long endTime) {
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(this, account)
                .readData(readRequest)
                .addOnSuccessListener(response -> {
                    int totalSteps = 0;
                    for (Bucket bucket : response.getBuckets()) {
                        for (DataSet dataSet : bucket.getDataSets()) {
                            for (DataPoint dp : dataSet.getDataPoints()) {
                                totalSteps += dp.getValue(Field.FIELD_STEPS).asInt();
                            }
                        }
                    }
                    textViewStepCount.setText("Steps: " + totalSteps);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Step Count fetch failed", e);
                    textViewStepCount.setText("Steps: Error");
                });
    }


    private void fetchCalories(GoogleSignInAccount account, long startTime, long endTime) {
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(this, account)
                .readData(readRequest)
                .addOnSuccessListener(response -> {
                    float totalCalories = 0;
                    for (Bucket bucket : response.getBuckets()) {
                        for (DataSet dataSet : bucket.getDataSets()) {
                            for (DataPoint dp : dataSet.getDataPoints()) {
                                totalCalories += dp.getValue(Field.FIELD_CALORIES).asFloat();
                            }
                        }
                    }
                    textViewCalories.setText("Calories Burned: " + (int) totalCalories + " kcal");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Calories fetch failed", e);
                    textViewCalories.setText("Calories: Error");
                });
    }


    private void navigateToHealthStatsActivity() {
        startActivity(new Intent(this, HealthStatsActivity.class));
    }
}

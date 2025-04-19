package com.example.pulseguard.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.SensorRequest;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GoogleFitHelper {
    private static final String TAG = "GoogleFitHelper";
    private static final int REQUEST_ACTIVITY_RECOGNITION = 100;

    private final Activity activity;
    private HealthDataListener healthDataListener;

    public interface HealthDataListener {
        void onDataReceived(String label, float value);
    }

    public GoogleFitHelper(Activity activity) {
        this.activity = activity;
    }

    public void setHealthDataListener(HealthDataListener listener) {
        this.healthDataListener = listener;
    }

    public void requestGoogleFitPermissions() {
        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                .build();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
        if (account == null) {
            Log.e(TAG, "Google Sign-In required before accessing Google Fit data.");
            return;
        }

        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(activity, 1, account, fitnessOptions);
        } else {
            startLiveHealthTracking();
            fetchTodayHealthData(); // fetch today's data instead of historical
        }
    }

    public void requestActivityRecognitionPermission() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    REQUEST_ACTIVITY_RECOGNITION);
        }
    }

    public void startLiveHealthTracking() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
        if (account == null) {
            Log.e(TAG, "Google Sign-In required before accessing Google Fit data.");
            return;
        }

        Fitness.getSensorsClient(activity, account)
                .findDataSources(new DataSourcesRequest.Builder()
                        .setDataTypes(DataType.TYPE_HEART_RATE_BPM, DataType.TYPE_STEP_COUNT_DELTA, DataType.TYPE_CALORIES_EXPENDED)
                        .setDataSourceTypes(DataSource.TYPE_DERIVED)
                        .build())
                .addOnSuccessListener(dataSources -> {
                    if (dataSources != null && !dataSources.isEmpty()) {
                        for (DataSource dataSource : dataSources) {
                            Log.d(TAG, "Found Data Source: " + dataSource.getDataType().getName());
                            registerLiveSensorListener(dataSource);
                        }
                    } else {
                        Log.e(TAG, "No Data Sources found.");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error finding data sources", e));
    }

    private void registerLiveSensorListener(DataSource dataSource) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
        if (account == null) return;

        Fitness.getSensorsClient(activity, account)
                .add(new SensorRequest.Builder()
                                .setDataSource(dataSource)
                                .setDataType(dataSource.getDataType())
                                .setSamplingRate(1, TimeUnit.SECONDS)
                                .build(),
                        this::handleLiveSensorData)
                .addOnSuccessListener(unused -> Log.d(TAG, "Live sensor listener registered successfully!"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to register live sensor listener", e));
    }

    private void handleLiveSensorData(DataPoint dataPoint) {
        for (Field field : dataPoint.getDataType().getFields()) {
            String label = field.getName();
            float value = dataPoint.getValue(field).asFloat();
            Log.d(TAG, "Live Data - " + label + ": " + value);
            updateLiveUI(label, value);
        }
    }

    public void fetchTodayHealthData() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
        if (account == null) {
            Log.e(TAG, "Google Sign-In required before accessing Google Fit data.");
            return;
        }

        fetchTodayData(DataType.AGGREGATE_STEP_COUNT_DELTA, "Step Count");
        fetchTodayData(DataType.AGGREGATE_CALORIES_EXPENDED, "Calories Burned");
        fetchTodayData(DataType.AGGREGATE_HEART_RATE_SUMMARY, "Heart Rate");
    }

    private void fetchTodayData(DataType aggregateType, String label) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
        if (account == null) {
            Log.e(TAG, "Google Sign-In required before accessing Google Fit data.");
            return;
        }

        // Start of today
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startTime = cal.getTimeInMillis();
        long endTime = System.currentTimeMillis();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(getRawTypeFromAggregate(aggregateType), aggregateType)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(activity, account)
                .readData(readRequest)
                .addOnSuccessListener(response -> {
                    float total = 0;
                    if (!response.getBuckets().isEmpty()) {
                        for (DataSet dataSet : response.getBuckets().get(0).getDataSets()) {
                            for (DataPoint dp : dataSet.getDataPoints()) {
                                for (Field field : dp.getDataType().getFields()) {
                                    float value = dp.getValue(field).asFloat();
                                    total += value;
                                }
                            }
                        }
                    }
                    Log.d(TAG, label + ": " + total);
                    updateHistoricalUI(label, total);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch " + label, e);
                    Toast.makeText(activity, "Failed to fetch " + label + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private DataType getRawTypeFromAggregate(DataType aggregateType) {
        if (aggregateType == DataType.AGGREGATE_STEP_COUNT_DELTA) return DataType.TYPE_STEP_COUNT_DELTA;
        if (aggregateType == DataType.AGGREGATE_CALORIES_EXPENDED) return DataType.TYPE_CALORIES_EXPENDED;
        if (aggregateType == DataType.AGGREGATE_HEART_RATE_SUMMARY) return DataType.TYPE_HEART_RATE_BPM;
        return aggregateType;
    }

    private void updateLiveUI(String label, float value) {
        if (healthDataListener != null) {
            healthDataListener.onDataReceived(label, value);
        }
    }

    private void updateHistoricalUI(String label, float value) {
        if (healthDataListener != null) {
            healthDataListener.onDataReceived(label, value);
        }
    }
}

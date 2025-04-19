package com.example.pulseguard.utils;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

    public void requestGoogleFitPermissions() {
        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                .build();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(activity, 1, account, fitnessOptions);
        } else {
            startLiveHealthTracking();
            fetchHistoricalHealthData();
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
            updateLiveUI(label, value); // Call renamed method for live data
        }
    }

    public void fetchHistoricalHealthData() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
        if (account == null) {
            Log.e(TAG, "Google Sign-In required before accessing Google Fit data.");
            return;
        }

        fetchData(DataType.TYPE_STEP_COUNT_DELTA, "Step Count");
        fetchData(DataType.TYPE_CALORIES_EXPENDED, "Calories Burned");
        fetchData(DataType.TYPE_HEART_RATE_BPM, "Heart Rate");
    }

    private void fetchData(DataType dataType, String label) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
        if (account == null) {
            Log.e(TAG, "Google Sign-In required before accessing Google Fit data.");
            return;
        }

        // Get current time in milliseconds
        long currentTimeMillis = System.currentTimeMillis();

        // Calculate the start and end times for yesterday
        long endOfYesterday = currentTimeMillis - (currentTimeMillis % TimeUnit.DAYS.toMillis(1)); // End of yesterday (midnight)
        long startOfYesterday = endOfYesterday - TimeUnit.DAYS.toMillis(1); // Start of yesterday (midnight of the previous day)

        // Create a DataReadRequest with yesterday's date range
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .read(dataType)
                .setTimeRange(startOfYesterday, endOfYesterday, TimeUnit.MILLISECONDS)
                .build();

        // Fetch the data from Google Fit
        Fitness.getHistoryClient(activity, account)
                .readData(readRequest)
                .addOnSuccessListener(response -> {
                    List<DataSet> dataSets = response.getDataSets();
                    for (DataSet dataSet : dataSets) {
                        float value = getDataPointValue(dataSet, dataType);
                        Log.d(TAG, label + ": " + value);
                        updateHistoricalUI(label, value); // Call renamed method for historical data
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to fetch " + label, e));
    }

    private float getDataPointValue(DataSet dataSet, DataType dataType) {
        return (dataSet == null || dataSet.getDataPoints().isEmpty()) ? 0 : dataSet.getDataPoints().get(0).getValue(dataType.getFields().get(0)).asFloat();
    }

    public void setHealthDataListener(HealthDataListener listener) {
        this.healthDataListener = listener;
    }

    // Renamed method for live data
    private void updateLiveUI(String label, float value) {
        if (healthDataListener != null) {
            healthDataListener.onDataReceived(label, value);
        }
    }

    // Renamed method for historical data
    private void updateHistoricalUI(String label, float value) {
        if (healthDataListener != null) {
            healthDataListener.onDataReceived(label, value);
        }
    }
}

package com.example.pulseguard.utils;

import android.app.Activity;
import android.util.Log;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class GoogleFitHelper {
    private static final String TAG = "GoogleFitHelper";
    private static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1;
    private Activity activity;

    public GoogleFitHelper(Activity activity) {
        this.activity = activity;
    }

    public void requestGoogleFitPermissions() {
        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
                .build();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);

        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(activity, GOOGLE_FIT_PERMISSIONS_REQUEST_CODE, account, fitnessOptions);
        } else {
            fetchHealthData();
        }
    }

    public void fetchHealthData() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
        if (account == null) {
            Log.e(TAG, "Google Sign-In required before accessing Google Fit data.");
            return;
        }

        // Fetch Steps Count
        Fitness.getHistoryClient(activity, account)
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(dataSet -> {
                    int steps = dataSet.isEmpty() ? 0 : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                    Log.d(TAG, "Steps Today: " + steps);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching steps data", e));

        // Fetch Calories Burned
        Fitness.getHistoryClient(activity, account)
                .readDailyTotal(DataType.TYPE_CALORIES_EXPENDED)
                .addOnSuccessListener(dataSet -> {
                    float calories = dataSet.isEmpty() ? 0 : dataSet.getDataPoints().get(0).getValue(Field.FIELD_CALORIES).asFloat();
                    Log.d(TAG, "Calories Burned: " + calories);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching calories data", e));

        // Fetch Heart Rate
        Fitness.getHistoryClient(activity, account)
                .readDailyTotal(DataType.TYPE_HEART_RATE_BPM)
                .addOnSuccessListener(dataSet -> {
                    float heartRate = dataSet.isEmpty() ? 0 : dataSet.getDataPoints().get(0).getValue(Field.FIELD_BPM).asFloat();
                    Log.d(TAG, "Heart Rate: " + heartRate);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching heart rate data", e));
    }
}

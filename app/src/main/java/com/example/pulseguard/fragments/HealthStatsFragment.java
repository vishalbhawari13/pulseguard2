package com.example.pulseguard.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.pulseguard.R;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.concurrent.TimeUnit;

public class HealthStatsFragment extends Fragment {

    private TextView heartRateTextView;
    private TextView stepsTextView;
    private TextView caloriesTextView;

    private static final int REQUEST_CODE_GOOGLE_FIT = 1001;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_health_stats, container, false);

        // Initialize UI components
        heartRateTextView = rootView.findViewById(R.id.heart_rate_text);
        stepsTextView = rootView.findViewById(R.id.steps_text);
        caloriesTextView = rootView.findViewById(R.id.calories_text);

        // Initialize Google Fit permissions
        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(getActivity());
        if (googleSignInAccount != null) {
            FitnessOptions fitnessOptions = FitnessOptions.builder()
                    .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                    .build();

            if (!GoogleSignIn.hasPermissions(googleSignInAccount, fitnessOptions)) {
                GoogleSignIn.requestPermissions(
                        this,
                        REQUEST_CODE_GOOGLE_FIT,
                        googleSignInAccount,
                        fitnessOptions);
            } else {
                accessGoogleFitData(googleSignInAccount);
            }
        }

        return rootView;
    }

    // Handle Google Fit data
    public void accessGoogleFitData(GoogleSignInAccount account) {
        // Access heart rate data
        Fitness.getHistoryClient(getActivity(), account)
                .readData(new com.google.android.gms.fitness.request.DataReadRequest.Builder()
                        .read(DataType.TYPE_HEART_RATE_BPM)
                        .setTimeRange(1, System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                        .build())
                .addOnSuccessListener(dataReadResponse -> {
                    for (DataSet dataSet : dataReadResponse.getDataSets()) {
                        for (DataPoint dataPoint : dataSet.getDataPoints()) {
                            // Extract and display heart rate data
                            float heartRate = dataPoint.getValue(Field.FIELD_INTENSITY).asFloat();
                            heartRateTextView.setText("Heart Rate: " + heartRate + " BPM");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    heartRateTextView.setText("Error fetching heart rate data");
                });

        // Access step count data
        Fitness.getHistoryClient(getActivity(), account)
                .readData(new com.google.android.gms.fitness.request.DataReadRequest.Builder()
                        .read(DataType.TYPE_STEP_COUNT_DELTA)
                        .setTimeRange(1, System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                        .build())
                .addOnSuccessListener(dataReadResponse -> {
                    for (DataSet dataSet : dataReadResponse.getDataSets()) {
                        for (DataPoint dataPoint : dataSet.getDataPoints()) {
                            // Extract and display step count data
                            Value stepCount = dataPoint.getValue(Field.FIELD_STEPS);
                            stepsTextView.setText("Steps: " + stepCount);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    stepsTextView.setText("Error fetching step count data");
                });

        // Access calories data
        Fitness.getHistoryClient(getActivity(), account)
                .readData(new com.google.android.gms.fitness.request.DataReadRequest.Builder()
                        .read(DataType.TYPE_CALORIES_EXPENDED)
                        .setTimeRange(1, System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                        .build())
                .addOnSuccessListener(dataReadResponse -> {
                    for (DataSet dataSet : dataReadResponse.getDataSets()) {
                        for (DataPoint dataPoint : dataSet.getDataPoints()) {
                            // Extract and display calories burned data
                            float calories = dataPoint.getValue(Field.FIELD_CALORIES).asFloat();
                            caloriesTextView.setText("Calories: " + calories + " kcal");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    caloriesTextView.setText("Error fetching calories data");
                });
    }

    // Handle the result of Google Fit permission request
    @Override
    public void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GOOGLE_FIT) {
            if (resultCode == getActivity().RESULT_OK) {
                GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(getActivity());
                if (googleSignInAccount != null) {
                    accessGoogleFitData(googleSignInAccount);
                }
            }
        }
    }
}

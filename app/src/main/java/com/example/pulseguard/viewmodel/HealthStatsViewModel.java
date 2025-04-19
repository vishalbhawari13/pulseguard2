package com.example.pulseguard.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HealthStatsViewModel extends ViewModel {
    private final MutableLiveData<String> sleepDuration = new MutableLiveData<>("0h 0m");
    private final MutableLiveData<String> stepCount = new MutableLiveData<>("0");
    private final MutableLiveData<String> heartRate = new MutableLiveData<>("0 BPM");
    private final MutableLiveData<String> caloriesBurned = new MutableLiveData<>("0 kcal");

    // LiveData Getters
    public LiveData<String> getSleepDuration() {
        return sleepDuration;
    }

    public LiveData<String> getStepCount() {
        return stepCount;
    }

    public LiveData<String> getHeartRate() {
        return heartRate;
    }

    public LiveData<String> getCaloriesBurned() {
        return caloriesBurned;
    }

    // Update Methods
    public void updateSleepDuration(int hours, int minutes) {
        sleepDuration.postValue(hours + "h " + minutes + "m");
    }

    public void updateStepCount(int steps) {
        stepCount.postValue(String.valueOf(steps));
    }

    public void updateHeartRate(int bpm) {
        heartRate.postValue(bpm + " BPM");
    }

    public void updateCaloriesBurned(int calories) {
        caloriesBurned.postValue(calories + " kcal");
    }

    // Method to reset all values (optional)
    public void resetStats() {
        sleepDuration.postValue("0h 0m");
        stepCount.postValue("0");
        heartRate.postValue("0 BPM");
        caloriesBurned.postValue("0 kcal");
    }

    // Method to update health data
    public void updateHealthData(String label, float value) {
        switch (label) {
            case "Step Count":
                updateStepCount((int) value);
                break;
            case "Heart Rate":
                updateHeartRate((int) value);
                break;
            case "Calories Burned":
                updateCaloriesBurned((int) value);
                break;
            case "Sleep Duration":
                int hours = (int) value / 60;
                int minutes = (int) value % 60;
                updateSleepDuration(hours, minutes);
                break;
            default:
                break;
        }
    }
}

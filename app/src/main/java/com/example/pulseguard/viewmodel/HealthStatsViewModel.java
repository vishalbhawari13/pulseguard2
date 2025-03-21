package com.example.pulseguard.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HealthStatsViewModel extends ViewModel {
    private final MutableLiveData<String> sleepDuration = new MutableLiveData<>("0h 0m");
    private final MutableLiveData<String> stepCount = new MutableLiveData<>("0");
    private final MutableLiveData<String> heartRate = new MutableLiveData<>("0 BPM");
    private final MutableLiveData<String> caloriesBurned = new MutableLiveData<>("0 kcal");

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
}

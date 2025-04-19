package com.example.pulseguard.models;

public class HealthData {
    private float heartRate;
    private int stepCount;
    private float caloriesBurned;

    public HealthData(float heartRate, int stepCount, float caloriesBurned) {
        this.heartRate = heartRate;
        this.stepCount = stepCount;
        this.caloriesBurned = caloriesBurned;
    }

    public float getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(float heartRate) {
        this.heartRate = heartRate;
    }

    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }

    public float getCaloriesBurned() {
        return caloriesBurned;
    }

    public void setCaloriesBurned(float caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }
}

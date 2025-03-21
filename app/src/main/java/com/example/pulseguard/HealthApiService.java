package com.example.pulseguard;
import retrofit2.Call;
import retrofit2.http.GET;

public interface HealthApiService {
    @GET("api/health-data") // Replace with your actual endpoint
    Call<HealthData> getHealthStats();
}

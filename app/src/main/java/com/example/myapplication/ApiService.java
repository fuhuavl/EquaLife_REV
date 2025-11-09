package com.example.myapplication;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @POST("v1beta/models/gemini-pro:generateContent")
    Call<GeminiResponse> getChatCompletion(
            @Body GeminiRequest requestBody,
            @Query("key") String apiKey
    );
}

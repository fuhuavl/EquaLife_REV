package com.example.myapplication;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    // Endpoint Senopati untuk chat text
    @POST("chat")
    Call<SenopatiResponse> getChatCompletion(@Body ApiRequest requestBody);
}
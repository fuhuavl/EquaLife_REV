package com.example.myapplication;

import java.util.List;

public class ApiRequest {
    String model;
    List<Message> messages;

    public ApiRequest(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
    }
}
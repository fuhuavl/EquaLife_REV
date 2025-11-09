package com.example.myapplication;

import java.util.List;

public class GeminiRequest {
    List<Content> contents;

    public GeminiRequest(List<Content> contents) {
        this.contents = contents;
    }
}
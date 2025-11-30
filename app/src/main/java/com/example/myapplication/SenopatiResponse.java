package com.example.myapplication;

public class SenopatiResponse {
    public String content;
    public String model;
    public Usage usage;

    public static class Usage {
        public int prompt_tokens;
        public int completion_tokens;
        public int total_tokens;
    }
}
package com.example.myapplication;

import java.util.List;

public class SenopatiResponse {
    public boolean success;
    public String error;
    public Data data;

    public static class Data {
        public String reply;
        public List<Message> messages;
    }
}
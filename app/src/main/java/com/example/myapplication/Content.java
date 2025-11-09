package com.example.myapplication;

import java.util.List;

public class Content {
    List<Part> parts;
    String role;

    public Content(List<Part> parts, String role) {
        this.parts = parts;
        this.role = role;
    }
}
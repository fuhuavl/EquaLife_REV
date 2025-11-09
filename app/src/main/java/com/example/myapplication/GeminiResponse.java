package com.example.myapplication;

import java.util.List;

public class GeminiResponse {
    List<Candidate> candidates;
    public String getResponseText() {
        if (candidates != null && !candidates.isEmpty()) {
            Candidate firstCandidate = candidates.get(0);
            if (firstCandidate.content != null && firstCandidate.content.parts != null && !firstCandidate.content.parts.isEmpty()) {
                return firstCandidate.content.parts.get(0).text;
            }
        }
        return null;
    }
}
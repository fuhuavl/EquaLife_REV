package com.example.myapplication;

public class Task {
    // --- TAMBAHAN BARU ---
    private long id;
    // --- BATAS TAMBAHAN ---

    private String taskName;
    private String date;
    private String startTime;
    private String endTime;

    // --- CONSTRUCTOR DIUBAH ---
    public Task(long id, String taskName, String date, String startTime, String endTime) {
        this.id = id;
        this.taskName = taskName;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // --- GETTER BARU ---
    public long getId() {
        return id;
    }
    // --- BATAS GETTER BARU ---

    // Getters (sisanya sama)
    public String getTaskName() {
        return taskName;
    }

    public String getDate() {
        return date;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getTimeRange() {
        return startTime + " - " + endTime;
    }
}
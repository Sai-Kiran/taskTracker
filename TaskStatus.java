package com.example.demo;

public enum TaskStatus {
    DONE,
    IN_PROGRESS,
    TODO;

    public static TaskStatus fromString(String name) {
        return TaskStatus.valueOf(name.toUpperCase());
    }
}


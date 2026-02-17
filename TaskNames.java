package com.example.demo;

public enum TaskNames {
    ADD,
    UPDATE,
    DELETE,
    MARK_IN_PROGRESS,
    MARK_DONE,
    LIST;

    public static TaskNames fromString(String name) {
        return TaskNames.valueOf(name.toUpperCase());
    }
}

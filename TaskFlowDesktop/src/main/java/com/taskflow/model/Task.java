package com.taskflow.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Task {
    public enum Priority {
        HIGH(1), MEDIUM(2), LOW(3);
        private final int rank;
        Priority(int rank) { this.rank = rank; }
        public int rank() { return rank; }
        @Override public String toString() {
            return switch (this) {
                case HIGH -> "High";
                case MEDIUM -> "Medium";
                case LOW -> "Low";
            };
        }
        public static Priority from(String value) {
            return Priority.valueOf(value.trim().toUpperCase());
        }
    }

    public enum Status {
        PENDING, IN_PROGRESS, COMPLETED;
        @Override public String toString() {
            return switch (this) {
                case PENDING -> "Pending";
                case IN_PROGRESS -> "In Progress";
                case COMPLETED -> "Completed";
            };
        }
        public static Status from(String value) {
            return switch (value.trim().toLowerCase()) {
                case "in progress" -> IN_PROGRESS;
                case "completed" -> COMPLETED;
                default -> PENDING;
            };
        }
    }

    private int id;
    private String title;
    private String description;
    private LocalDate deadline;
    private Priority priority;
    private Status status;
    private String category;
    private LocalDateTime createdAt;

    public Task(int id, String title, String description, LocalDate deadline,
                Priority priority, Status status, String category, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description == null ? "" : description;
        this.deadline = deadline;
        this.priority = priority;
        this.status = status;
        this.category = category == null || category.isBlank() ? "Other" : category.trim();
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getDeadline() { return deadline; }
    public Priority getPriority() { return priority; }
    public Status getStatus() { return status; }
    public String getCategory() { return category; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public void setStatus(Status status) { this.status = status; }
    public void setCategory(String category) { this.category = category; }

    public boolean isOverdue() {
        return status != Status.COMPLETED && deadline.isBefore(LocalDate.now());
    }
}
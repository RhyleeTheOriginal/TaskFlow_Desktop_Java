package com.taskflow.service;

import com.taskflow.db.TaskRepository;
import com.taskflow.model.Task;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class TaskService {
    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public List<Task> all() throws SQLException { return repository.findAll(); }

    public Task create(String title, String description, LocalDate deadline,
                       Task.Priority priority, String category) throws SQLException {
        validate(title, deadline, priority, category);
        repository.addCategory(category);
        Task task = new Task(0, title.trim(), description, deadline, priority,
                             Task.Status.PENDING, category, null);
        return repository.save(task);
    }

    public void update(Task task) throws SQLException {
        validate(task.getTitle(), task.getDeadline(), task.getPriority(), task.getCategory());
        repository.addCategory(task.getCategory());
        repository.save(task);
    }

    public void delete(int id) throws SQLException { repository.delete(id); }

    public List<Task> search(String query) throws SQLException {
        String q = query.toLowerCase(Locale.ROOT).trim();
        return all().stream()
            .filter(t -> t.getTitle().toLowerCase(Locale.ROOT).contains(q)
                || t.getDescription().toLowerCase(Locale.ROOT).contains(q)
                || t.getCategory().toLowerCase(Locale.ROOT).contains(q))
            .collect(Collectors.toList());
    }

    public List<String> categories() throws SQLException { return repository.findCategories(); }

    public void addCategory(String category) throws SQLException {
        if (category == null || category.isBlank()) throw new IllegalArgumentException("Category cannot be empty.");
        repository.addCategory(category);
    }

    private void validate(String title, LocalDate deadline, Task.Priority priority, String category) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Task title is required.");
        if (deadline == null) throw new IllegalArgumentException("A deadline is required.");
        if (priority == null) throw new IllegalArgumentException("A priority is required.");
        if (category == null || category.isBlank()) throw new IllegalArgumentException("A category is required.");
    }
}
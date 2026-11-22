package com.taskflow.db;

import com.taskflow.model.Task;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskRepository {
    public List<Task> findAll() throws SQLException {
        String sql = "SELECT * FROM tasks ORDER BY id DESC";
        List<Task> tasks = new ArrayList<>();
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) tasks.add(map(rs));
        }
        return tasks;
    }

    public Optional<Task> findById(int id) throws SQLException {
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM tasks WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Task save(Task task) throws SQLException {
        if (task.getId() == 0) {
            String sql = """
                INSERT INTO tasks(title,description,deadline,priority,status,category,created_at)
                VALUES(?,?,?,?,?,?,?)
            """;
            try (Connection c = Database.connect();
                 PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                bind(ps, task);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) task.setId(keys.getInt(1));
                }
            }
        } else {
            String sql = """
                UPDATE tasks SET title=?,description=?,deadline=?,priority=?,status=?,category=?
                WHERE id=?
            """;
            try (Connection c = Database.connect();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, task.getTitle());
                ps.setString(2, task.getDescription());
                ps.setString(3, task.getDeadline().toString());
                ps.setString(4, task.getPriority().name());
                ps.setString(5, task.getStatus().name());
                ps.setString(6, task.getCategory());
                ps.setInt(7, task.getId());
                ps.executeUpdate();
            }
        }
        return task;
    }

    private void bind(PreparedStatement ps, Task t) throws SQLException {
        ps.setString(1, t.getTitle());
        ps.setString(2, t.getDescription());
        ps.setString(3, t.getDeadline().toString());
        ps.setString(4, t.getPriority().name());
        ps.setString(5, t.getStatus().name());
        ps.setString(6, t.getCategory());
        ps.setString(7, t.getCreatedAt().toString());
    }

    public void delete(int id) throws SQLException {
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement("DELETE FROM tasks WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<String> findCategories() throws SQLException {
        List<String> result = new ArrayList<>();
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement("SELECT name FROM categories ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(rs.getString(1));
        }
        return result;
    }

    public void addCategory(String name) throws SQLException {
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement("INSERT OR IGNORE INTO categories(name) VALUES(?)")) {
            ps.setString(1, name.trim());
            ps.executeUpdate();
        }
    }

    private Task map(ResultSet rs) throws SQLException {
        return new Task(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("description"),
            LocalDate.parse(rs.getString("deadline")),
            Task.Priority.valueOf(rs.getString("priority")),
            Task.Status.valueOf(rs.getString("status")),
            rs.getString("category"),
            LocalDateTime.parse(rs.getString("created_at"))
        );
    }
}
package com.taskflow.ui;

import com.taskflow.model.Task;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class TaskDialog {
    public static Task show(Component parent, List<String> categories, Task existing) {
        JTextField title = new JTextField(existing == null ? "" : existing.getTitle(), 24);
        JTextArea description = new JTextArea(existing == null ? "" : existing.getDescription(), 3, 24);
        JTextField deadline = new JTextField(existing == null ? LocalDate.now().plusDays(1).toString()
                                                              : existing.getDeadline().toString(), 12);
        JComboBox<Task.Priority> priority = new JComboBox<>(Task.Priority.values());
        JComboBox<Task.Status> status = new JComboBox<>(Task.Status.values());
        JComboBox<String> category = new JComboBox<>(categories.toArray(String[]::new));
        category.setEditable(true);

        if (existing != null) {
            priority.setSelectedItem(existing.getPriority());
            status.setSelectedItem(existing.getStatus());
            category.setSelectedItem(existing.getCategory());
        }

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.gridy = 0;
        panel.add(new JLabel("Task title *"), g);
        g.gridx = 1; panel.add(title, g);
        g.gridx = 0; g.gridy++; panel.add(new JLabel("Description"), g);
        g.gridx = 1; panel.add(new JScrollPane(description), g);
        g.gridx = 0; g.gridy++; panel.add(new JLabel("Deadline (YYYY-MM-DD) *"), g);
        g.gridx = 1; panel.add(deadline, g);
        g.gridx = 0; g.gridy++; panel.add(new JLabel("Priority"), g);
        g.gridx = 1; panel.add(priority, g);
        g.gridx = 0; g.gridy++; panel.add(new JLabel("Status"), g);
        g.gridx = 1; panel.add(status, g);
        g.gridx = 0; g.gridy++; panel.add(new JLabel("Category"), g);
        g.gridx = 1; panel.add(category, g);

        int result = JOptionPane.showConfirmDialog(parent, panel,
            existing == null ? "Create Task" : "Edit Task",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return null;

        try {
            if (title.getText().isBlank()) throw new IllegalArgumentException("Task title is required.");
            LocalDate date = LocalDate.parse(deadline.getText().trim());
            String cat = String.valueOf(category.getSelectedItem()).trim();
            if (cat.isBlank()) cat = "Other";
            return new Task(existing == null ? 0 : existing.getId(),
                title.getText().trim(), description.getText().trim(), date,
                (Task.Priority) priority.getSelectedItem(),
                (Task.Status) status.getSelectedItem(), cat,
                existing == null ? null : existing.getCreatedAt());
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(parent, "Use a valid date in YYYY-MM-DD format.",
                "Invalid date", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(),
                "Invalid task", JOptionPane.WARNING_MESSAGE);
        }
        return null;
    }
}
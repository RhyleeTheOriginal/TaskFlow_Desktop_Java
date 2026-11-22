package com.taskflow;

import com.formdev.flatlaf.FlatLightLaf;
import com.taskflow.db.Database;
import com.taskflow.db.TaskRepository;
import com.taskflow.service.TaskService;
import com.taskflow.ui.MainFrame;
import com.taskflow.ui.ThemeManager;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            Database.initialize();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                "TaskFlow could not initialize its database:\n" + ex.getMessage(),
                "Startup error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ThemeManager.install(false);
        SwingUtilities.invokeLater(() -> {
            TaskService service = new TaskService(new TaskRepository());
            new MainFrame(service).setVisible(true);
        });
    }
}
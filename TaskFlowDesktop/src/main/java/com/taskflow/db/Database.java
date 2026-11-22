package com.taskflow.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Database {
    private static final String DB_DIRECTORY = "data";
    private static final String DB_URL = "jdbc:sqlite:" + DB_DIRECTORY + "/taskflow.db";

    private Database() {}

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initialize() throws Exception {
        Files.createDirectories(Path.of(DB_DIRECTORY));
        try (Connection connection = connect();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS tasks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    description TEXT NOT NULL DEFAULT '',
                    deadline TEXT NOT NULL,
                    priority TEXT NOT NULL CHECK(priority IN ('HIGH','MEDIUM','LOW')),
                    status TEXT NOT NULL CHECK(status IN ('PENDING','IN_PROGRESS','COMPLETED')),
                    category TEXT NOT NULL,
                    created_at TEXT NOT NULL
                )
            """);
            statement.execute("CREATE INDEX IF NOT EXISTS idx_tasks_deadline ON tasks(deadline)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status)");
            statement.execute("""
                CREATE TABLE IF NOT EXISTS categories (
                    name TEXT PRIMARY KEY COLLATE NOCASE
                )
            """);
            statement.execute("""
                INSERT OR IGNORE INTO categories(name)
                VALUES ('School'), ('Personal'), ('Work'), ('Other')
            """);
        }
    }
}
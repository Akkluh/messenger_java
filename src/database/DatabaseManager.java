package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:chat.db";
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }
    public static void initializeDatabase() {
        System.out.println("Database initialized");
        String sql = "CREATE TABLE IF NOT EXISTS messages (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL," +
                "message TEXT NOT NULL," +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ");";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

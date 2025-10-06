package main.dash.database.user;

import java.sql.*;
import main.dash.database.Database;

public class userActionsDatabase {
    /**
     * URL which connects to the database containing the user information
     */
    private static final String databaseURL = "jdbc:sqlite:databases/userActions.db"; // Database file

    /**
     * Creates table with columns for unique action id, username, and action
     */
    public static void createTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS userActions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL,
                    userAction TEXT NOT NULL
                );
                """;

        Database.tableOperation(databaseURL, sql);
    }

    /**
     * Deletes table containing user actions
     */
    public static void dropTable() {
        Database.dropTable(databaseURL, "userActions");
    }

    /**
     * Function to insert a new user action into the database
     * @param username User's username
     * @param userAction Action carried out by the user
     */
    public static void insertUser(String username, String userAction) {
        // By structuring the SQL statement in this way, SQL injections are prevented.
        String sql = "INSERT INTO userActions (username, userAction) VALUES (?, ?)";

        try (Connection conn = Database.connect(databaseURL)) { assert conn != null;
            try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, userAction);
                Database.updateTable(databaseURL, preparedStatement);
            }
        } catch (SQLException e) {
            System.out.println("Error inserting user action: " + e.getMessage());
        }
    }
}

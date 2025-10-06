package main.dash.database.user;

import java.sql.*;
import javafx.scene.chart.PieChart.Data;
import main.dash.database.Database;
import org.mindrot.jbcrypt.BCrypt;

public class usersDatabase {
  /**
   * URL which connects to the database containing the user information
   */
  private static final String databaseURL = "jdbc:sqlite:databases/users.db"; // Database file

  /**
   * Creates table with columns for unique user id, username, password and role
   */
  public static void createTable() {
    String sql =
        """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    userRole TEXT NOT NULL
                );
                """;

    Database.tableOperation(databaseURL, sql);
  }

  public static void dropTable() {
    Database.dropTable(databaseURL, "users");
  }

  /**
   * Function to insert a new user into the database
   * @param username User's username
   * @param password User's password
   * @param userRole User's role
   */
  public static void insertUser(String username, String password, String userRole) {
    // Hash the password using the BCrypt hashing algorithm, to ensure that if the database
    // is breached, compromised passwords are effectively unusable
    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
    // By structuring the SQL statement in this way, SQL injections are prevented.
    String sql = "INSERT INTO users (username, password, userRole) VALUES (?, ?, ?)";

    try (Connection conn = Database.connect(databaseURL)) {
      assert conn != null;
      try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, hashedPassword);
        preparedStatement.setString(3, userRole);
        Database.updateTable(databaseURL, preparedStatement);
      }
    } catch (SQLException e) {
      System.out.println("Error inserting user: " + e.getMessage());
    }
  }

  /**
   * Removes a user from the database
   * @param username Username of user to be removed
   */
  public static void removeUser(String username) {
    String sql = "DELETE FROM users WHERE username = ?";

    try (Connection conn = Database.connect(databaseURL)) {
      assert conn != null;
      try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
        preparedStatement.setString(1, username);
        // Checks whether any users were found and deleted
        if (Database.updateTable(databaseURL, preparedStatement) > 0) {
          System.out.println("User deleted");
        } else {
          System.out.println("User not found");
        }
      }
    } catch (SQLException e) {
      System.out.println("Error removing user: " + e.getMessage());
    }
  }

  /**
   * Returns the role of the specified user
   * @param username Username of the user
   * @return Role of the user
   */
  public static String getRole(String username) {
    String sql = "SELECT userRole FROM users WHERE username = ?";

    try (Connection conn = Database.connect(databaseURL)) {
      assert conn != null;
      try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
        preparedStatement.setString(1, username);
        ResultSet userInfo = Database.queryTable(databaseURL, preparedStatement);
        assert userInfo != null;
        if (userInfo.next()) {
          return userInfo.getString("userRole");
        } else {
          System.out.println("User not found");
        }
      }
    } catch (SQLException e) {
      System.out.println("Error getting user role: " + e.getMessage());
    }

    return null;
  }

  /**
   * Function to be called when a user tries to log in
   * @param username Entered username
   * @param password Entered password
   * @return Boolean value indicating whether the user can be authenticated or not
   */
  public static boolean authenticateUser(String username, String password) {
    String sql = "SELECT password FROM users WHERE username = ?";

    try (Connection conn = Database.connect(databaseURL)) {
      assert conn != null;
      try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

        preparedStatement.setString(1, username);
        ResultSet userInfo = Database.queryTable(databaseURL, preparedStatement);

        assert userInfo != null;
        if (userInfo.next()) {
          String hashedPassword = userInfo.getString("password"); // Retrieves hashed password from database
          if (BCrypt.checkpw(password, hashedPassword)) { // Checks if the hash of the input password is the same as the
                                 // hashed
            // password retrieved from the database
            System.out.println("Login successful");
            return true;
          } else {
            System.out.println("Invalid password");
          }
        } else {
          System.out.println("User not found");
        }
      }
    } catch (SQLException e) {
      System.out.println("Error authenticating user: " + e.getMessage());
    }

    return false;
  }

  /**
   * Changes the role of the specified user
   * @param username Username of specified user
   * @param newUserRole New role to be assigned to specified user
   */
  public static void changeUserRole(String username, String newUserRole) {
    String sql = "UPDATE users SET userRole = ? WHERE username = ?";

    try (Connection conn = Database.connect(databaseURL)) {
      assert conn != null;
      try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
        preparedStatement.setString(1, newUserRole);
        preparedStatement.setString(2, username);
        // If any roles are changed
        if (Database.updateTable(databaseURL, preparedStatement) > 0) {
          System.out.println("User role updated successfully");
        } else {
          System.out.println("User not found.");
        }
      }
    } catch (SQLException e) {
      System.out.println("Error changing user role: " + e.getMessage());
    }
  }

  /**
   * Gets number of users in the database
   * @return Number of users in users database
   */
  public static int getNumberOfUsers() {
    String sql = "SELECT COUNT(*) AS total FROM users";

      try (Connection conn = Database.connect(databaseURL)) { assert conn != null;
          try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
              ResultSet totalUsers = Database.queryTable(databaseURL, preparedStatement);
              assert totalUsers != null;
              if (totalUsers.next()) {
                  return totalUsers.getInt("total");
              }
          }
      } catch (SQLException e) {
          System.out.println("Error getting number of users: " + e.getMessage());
      }

    return 0;
  }

  public static void main(String[] args) {
    removeUser("admin");
    insertUser("admin", "password", "Admin");
  }
}

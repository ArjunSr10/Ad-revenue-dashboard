package main.dash.database;

import java.sql.*;
import javafx.scene.chart.PieChart.Data;

public class Database {

	/**
	 * Function to return the connection to the database
	 * @return Connection to the database. If this is returned, connection to the database was successful
	 */
	public static Connection connect(String databaseURL) {
		try {
			return DriverManager.getConnection(databaseURL);
		} catch (SQLException e) {
			System.out.println("Connection failed: " + e.getMessage());
			return null;
		}
	}

	private static String getName(String databaseURL){
		return databaseURL.split(":")[2].split("\\.")[0];
	}

	/**
	 * drop table
	 * @param databaseURL  the url of database
	 * @param name the name of table
	 */
	public static void dropTable(String databaseURL, String name) {
		String sql = "DROP TABLE IF EXISTS " + name;

		tableOperation(databaseURL, sql);
	}

	/**
	 * select table
	 * @param databaseURL the url of database
	 * @param preparedStatement a prepared statement
	 * @return
	 */
	public static ResultSet queryTable(String databaseURL, PreparedStatement preparedStatement){

		try{
			ResultSet result = preparedStatement.executeQuery();
			System.out.printf("Query executed on %s table\n", getName(databaseURL) + "\n\n" + preparedStatement);

			return result;
		} catch (SQLException e) {
			System.out.println("Error executing query: " + e.getMessage());
			return null;
		}
	}

	/**
	 * update table
	 * @param databaseURL the url of database
	 * @param preparedStatement a prepared statement
	 * @return
	 */
	public static int updateTable(String databaseURL, PreparedStatement preparedStatement){
		try{
				System.out.printf("Update executed on %s table\n", getName(databaseURL));
				return preparedStatement.executeUpdate();

		} catch (SQLException e) {
			System.out.println("Error updating table: " + e.getMessage());
			return 0;
		}
	}


	/**
	 * execute sql,like select,update,delete,insert,drop
	 * @param databaseURL  the url of database
	 * @param sql SQL statements that need to be executed
	 */
	public static void tableOperation(String databaseURL, String sql){
		try (Connection conn = connect(databaseURL)) {
			assert conn != null;
			try (Statement statement = conn.createStatement()) {
				statement.execute(sql);
				System.out.printf("Table operation executed on %s table\n", getName(databaseURL));
			}
		} catch (SQLException e) {
			System.out.println("Error executing table operation: " + e.getMessage());
		}
	}
}

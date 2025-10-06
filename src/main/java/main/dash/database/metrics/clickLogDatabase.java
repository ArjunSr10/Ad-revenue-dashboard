package main.dash.database.metrics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import main.dash.database.Database;
import main.dash.scene.DataView;

public class clickLogDatabase {

	private static DataView view;

	/**
	 * URL which connects to the database containing the user information
	 */
	private static final String databaseURL = "jdbc:sqlite:databases/metrics.db"; // Database file

	/**
	 * Creates table with columns for unique action id, username, and action
	 */
	public static void createTable() {
		String sql = """
                CREATE TABLE IF NOT EXISTS clickLog (
                    Date DATETIME NOT NULL,
                    ID TEXT NOT NULL,
                    ClickCost DOUBLE NOT NULL,
                    PRIMARY KEY (Date, ID)
                );
                
                CREATE INDEX IF NOT EXISTS cLog_ID ON clickLog(ID);
                """;

		Database.tableOperation(databaseURL, sql);
	}

	/**
	 * Deletes table containing user actions
	 */
	public static void dropTable() {
		Database.dropTable(databaseURL, "clickLog");
	}

	public static void importData(String path) {
		new Thread(() -> {
			view.showUploadLoading(true);

			try (Connection conn = Database.connect(databaseURL)) { assert conn != null;
				//Perform operations as a single transaction
				conn.setAutoCommit(false);
				try (PreparedStatement preparedStatement = conn.prepareStatement("DELETE FROM clickLog")) {
					Database.updateTable(databaseURL, preparedStatement);
				}

				String sql = "INSERT INTO clickLog (Date, ID, ClickCost) VALUES (?, ?, ?)";

				try(BufferedReader reader = new BufferedReader(new FileReader(path))) {
					String line;
					PreparedStatement preparedStatement = conn.prepareStatement(sql);

					// Skip the first line
					reader.readLine();
					while ((line = reader.readLine()) != null) {
						String[] data = line.split(",");
						preparedStatement.setString(1, data[0]);
						preparedStatement.setString(2, data[1]);
						preparedStatement.setDouble(3, Double.parseDouble(data[2]));

						preparedStatement.addBatch();
					}

					preparedStatement.executeBatch();
					conn.commit();
				} catch (IOException e) {
					conn.rollback();
					throw new RuntimeException(e);
				}
				try (PreparedStatement preparedStatement = conn.prepareStatement("CREATE INDEX cLog_ID ON clickLog(ID);")) {
					Database.updateTable(databaseURL, preparedStatement);
				}
				finally{
					conn.setAutoCommit(true);
				}
			} catch (SQLException e) {
				System.out.println("Error importing data: " + e.getMessage());
			}

			view.showUploadLoading(false);
		}).start();
	}

	public static void setView(DataView view) {
		clickLogDatabase.view = view;
	}
}

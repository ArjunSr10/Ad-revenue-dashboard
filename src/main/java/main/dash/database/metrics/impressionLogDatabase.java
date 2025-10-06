package main.dash.database.metrics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import main.dash.database.Database;
import main.dash.scene.DataView;

public class impressionLogDatabase {

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
                CREATE TABLE IF NOT EXISTS impressionsLog (
                    Date DATETIME NOT NULL,
                    ID TEXT NOT NULL,
                    Gender TEXT NOT NULL,
                    Age TEXT NOT NULL,
                    Income TEXT NOT NULL,
                    Context TEXT NOT NULL,
                    ImpressionCost DOUBLE NOT NULL,
                    PRIMARY KEY (Date, ID, Context, ImpressionCost)
                );
                
                CREATE INDEX IF NOT EXISTS iLog_ID ON impressionsLog(ID);
                """;

		Database.tableOperation(databaseURL, sql);
	}

	/**
	 * Deletes table containing user actions
	 */
	public static void dropTable() {
		Database.dropTable(databaseURL, "impressionsLog");
	}


	public static void importData(String path) {
		new Thread(() -> {
			view.showUploadLoading(true);
			try (Connection conn = Database.connect(databaseURL)) { assert conn != null;
				//Perform operations as a single transaction
				conn.setAutoCommit(false);
				try (PreparedStatement preparedStatement = conn.prepareStatement("DELETE FROM impressionsLog")) {
					Database.updateTable(databaseURL, preparedStatement);
				}

				String sql = "INSERT INTO impressionsLog (Date, ID, Gender, Age, Income, Context, ImpressionCost) VALUES (?, ?, ?, ?, ?, ?, ?)";

				try(BufferedReader reader = new BufferedReader(new FileReader(path))) {
					String line;
					PreparedStatement preparedStatement = conn.prepareStatement(sql);

					// Skip the first line
					reader.readLine();

					int count = 0;
					while ((line = reader.readLine()) != null) {
						String[] data = line.split(",");
						preparedStatement.setString(1, data[0]);
						preparedStatement.setString(2, data[1]);
						preparedStatement.setString(3, data[2]);
						preparedStatement.setString(4, data[3]);
						preparedStatement.setString(5, data[4]);
						preparedStatement.setString(6, data[5]);
						preparedStatement.setDouble(7, Double.parseDouble(data[6]));

						preparedStatement.addBatch();
						count++;

						if(count % 100000 == 0){
							preparedStatement.executeBatch();
						}
					}

					preparedStatement.executeBatch();
					conn.commit();
				} catch (IOException e) {
					conn.rollback();
					throw new RuntimeException(e);
				}
				try (PreparedStatement preparedStatement = conn.prepareStatement("CREATE INDEX iLog_ID ON impressionsLog(ID);")) {
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
		impressionLogDatabase.view = view;
	}
}

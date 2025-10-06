package main.dash.scene;

import java.io.File;
import java.sql.ResultSet;
import javafx.application.Application;
import javafx.stage.Stage;
import java.io.IOException;
import main.dash.data.DataController;
import main.dash.data.DataModel;
import main.dash.database.metrics.clickLogDatabase;
import main.dash.database.metrics.impressionLogDatabase;
import main.dash.database.metrics.serverLogDatabase;
import main.dash.enums.TimeGranularity;

/**
 * Program Entrance
 */
public class GUI extends Application {
	/**
	 * (1)load csv data file<p>
	 * (2)build data controller
	 * @param stage
	 */
	@Override
	public void start(Stage stage) {
		// Initialise MVC components
		DataView view = new DataView(stage);
		DataController controller = new DataController(stage, view);

		clickLogDatabase.createTable();
		impressionLogDatabase.createTable();
		serverLogDatabase.createTable();

		clickLogDatabase.setView(view);
		impressionLogDatabase.setView(view);
		serverLogDatabase.setView(view);
	}

	public static void main(String[] args) {
		launch(args);
	}
}

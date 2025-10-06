package main.dash.data;

import javafx.stage.Stage;
import main.dash.scene.DataView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.*;

class GUITest extends ApplicationTest {
    private DataModel model;
    private DataController controller;
    private DataView view;

    @Override
    public void start(Stage stage) {
        view = new DataView(stage); // Initialize GUI components in the JavaFX thread
    }

    @BeforeEach
    void setUp() {
        model = new DataModel();
    }
}
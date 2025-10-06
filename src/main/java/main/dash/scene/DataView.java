package main.dash.scene;

import java.net.URL;

import java.util.Objects;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.WindowEvent;
import main.dash.common.AlarmUtils;
import main.dash.common.Context;
import main.dash.database.user.usersDatabase;
import main.dash.enums.ColourScheme;
import main.dash.enums.Graph;

import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.TreeMap;

import main.dash.enums.Metric;
import main.dash.event.FileListener;
import main.dash.event.SceneListener;
import main.dash.event.TimeGranularityListener;
import main.dash.event.MetricListener;
import main.dash.event.FilterListener;

import main.dash.scene.controller.*;

import main.dash.enums.SceneName;

public class DataView {

    @FXML
    private Parent loginScreen, dashboardScreen, uploadScreen, settingsScreen, manualScreen, userManagerScreen;

    protected SceneListener sceneListener;
    protected MetricListener metricListener;
    protected TimeGranularityListener timeGranularityListener;
    protected ChangeListener<String> updateFieldListener;

    private Stage stage;

    private final Scene loginScene, dashboardScene, fileUploadScene, settingsScene, manualScene, userManagerScene;

    private final DashboardController dashboardController;
    private final LoginController loginController;
    private final SettingsController settingsController;
    private final UploadController uploadController;
    private final ManualController manualController;
    private final UserManagerController userManagerController;

    public DataView(Stage stage) {
        this.stage = stage;
        stage.setTitle("Ad Revenue Dashboard");

        URL loginFXML = getClass().getResource("/fxml/login.fxml");
        URL dashboardFXML = getClass().getResource("/fxml/dashboard.fxml");
        URL uploadFXML = getClass().getResource("/fxml/upload.fxml");
        URL settingsFXML = getClass().getResource("/fxml/settings.fxml");
        URL manualFXML = getClass().getResource("/fxml/manual.fxml");
        URL userManagerFXML = getClass().getResource("/fxml/userManager.fxml");

        try {
            FXMLLoader settingsLoader = new FXMLLoader(Objects.requireNonNull(settingsFXML));
            settingsScreen = settingsLoader.load();
            settingsController = settingsLoader.getController();
            settingsController.setView(this);

            FXMLLoader loginLoader = new FXMLLoader(Objects.requireNonNull(loginFXML));
            loginScreen = loginLoader.load();
            loginController = loginLoader.getController();
            loginController.setView(this);

            FXMLLoader uploadLoader = new FXMLLoader(Objects.requireNonNull(uploadFXML));
            uploadScreen = uploadLoader.load();
            uploadController = uploadLoader.getController();
            uploadController.setView(this);

            FXMLLoader dashboardLoader = new FXMLLoader(Objects.requireNonNull(dashboardFXML));
            dashboardScreen = dashboardLoader.load();
            dashboardController = dashboardLoader.getController();
            dashboardController.setView(this);

            FXMLLoader manualLoader = new FXMLLoader(Objects.requireNonNull(manualFXML));
            manualScreen = manualLoader.load();
            manualController = manualLoader.getController();
            manualController.setView(this);

            FXMLLoader userManagerLoader = new FXMLLoader(Objects.requireNonNull(userManagerFXML));
            userManagerScreen = userManagerLoader.load();
            userManagerController = userManagerLoader.getController();
            userManagerController.setView(this);

            // Get all the FXML elements
        } catch (Exception e) {
            throw new RuntimeException("Failed to load FXML file", e);
        }

        // Setting up scenes and stage
        loginScene = new Scene(loginScreen, 800, 600);
        dashboardScene = new Scene(dashboardScreen, 800, 600);
        fileUploadScene = new Scene(uploadScreen, 800, 600);
        settingsScene = new Scene(settingsScreen, 800, 600);
        manualScene = new Scene(manualScreen, 800, 600);
        userManagerScene = new Scene(userManagerScreen, 800, 600);

        setColourScheme(settingsController.getColourScheme());

        stage.setScene(loginScene);
        stage.setOnCloseRequest(event -> {
        });
        stage.show();
    }


    /**
     * The callback event when the exit button is clicked.<p>
     *  A confirmation box is displayed, clicking Yes will exit the program, otherwise it will not exit.
     * @param event window event
     */
    public void exit(WindowEvent event) {
        AlarmUtils.confirmation("Confirmation", "Are you sure you want to quit?", () -> {
            Platform.exit();
            System.exit(0);
            return null;
        }, () -> {
            if (event != null) event.consume();
            return null;
        });
        if (event != null) event.consume();
    }

    /**
     * set total text
     * @param graph the type of textfield set text
     * @param metric the data dimension
     */
    public void setTotalText(int graph, Metric metric) {
        dashboardController.setTotalText(graph, metric);
    }

    /**
     *
     * @param obj
     * @param multiplier
     */
    public void bindFont(Node obj, float multiplier) {
        StringBinding value = settingsController.getFontSize().valueProperty().multiply(multiplier).asString();
        ObservableValue<String> fontBinding = Bindings.concat("-fx-font-size: ", value, ";");
        obj.styleProperty().unbind();
        obj.styleProperty().bind(fontBinding);
    }

    /**
     *
     * @param colourScheme
     */
    public void setColourScheme(ColourScheme colourScheme) {
        setSceneColourScheme(loginScene, colourScheme);
        setSceneColourScheme(dashboardScene, colourScheme);
        setSceneColourScheme(fileUploadScene, colourScheme);
        setSceneColourScheme(settingsScene, colourScheme);
        setSceneColourScheme(manualScene, colourScheme);
        setSceneColourScheme(userManagerScene, colourScheme);
    }

    /**
     *
     * @param scene
     * @param colourScheme
     */
    private void setSceneColourScheme(Scene scene, ColourScheme colourScheme) {
        String darkMode = Objects.requireNonNull(getClass().getResource("/css/dark-mode.css")).toExternalForm();
        String lightMode = Objects.requireNonNull(getClass().getResource("/css/light-mode.css")).toExternalForm();
        switch (colourScheme) {
            case DARK:
                if (scene.getStylesheets().contains(darkMode)) return;
                scene.getStylesheets().remove(lightMode);
                scene.getStylesheets().add(darkMode);
                break;
            case LIGHT:
                if (scene.getStylesheets().contains(lightMode)) return;
                scene.getStylesheets().remove(darkMode);
                scene.getStylesheets().add(lightMode);
                break;
        }
    }

    /**
     *
     * @param listener
     */
    public void setOnSceneChange(SceneListener listener) {
        this.sceneListener = listener;

        loginController.handleEvents(sceneListener);
        settingsController.handleEvents(sceneListener);
        uploadController.handleEvents(sceneListener);
        dashboardController.handleEvents(sceneListener, metricListener, timeGranularityListener);
        manualController.handleEvents(sceneListener);
        userManagerController.handleEvents(sceneListener);
    }

    /**
     *
     * @param listener
     */
    public void setOnMetricApply(MetricListener listener) {
        this.metricListener = listener;
        dashboardController.handleEvents(sceneListener, metricListener, timeGranularityListener);
    }

    /**
     *
     * @param listener
     */
    public void setOnFieldUpdate(ChangeListener<String> listener) {
        this.updateFieldListener = listener;
        dashboardController.getBounceFieldsController().handleUpdates(updateFieldListener);
    }

    /**
     *
     * @param listener
     */
    public void setOnTimeGranularityApply(TimeGranularityListener listener) {
        this.timeGranularityListener = listener;
        dashboardController.handleEvents(sceneListener, metricListener, timeGranularityListener);
    }

    /**
     *
     * @param listener
     */
    public void setOnFileUpload(FileListener listener) {
        uploadController.setFileListener(listener);
    }

    /**
     *
     * @param label
     * @param viewIndex
     */
    public void setXAxis(String label, int viewIndex) {
        Platform.runLater(() -> {
            dashboardController.setLineXAxis(label, viewIndex);
            dashboardController.setBarXAxis(label, viewIndex);
        });
    }

    /**
     *
     * @param label
     * @param viewIndex
     */
    public void setYAxis(String label, int viewIndex) {
        Platform.runLater(() -> {
            dashboardController.setLineYAxis(label, viewIndex);
            dashboardController.setBarYAxis(label, viewIndex);
        });
    }

    /**
     *
     */
    public void hideMetric2Button() {
        Platform.runLater(dashboardController::hideMetric2Button);
    }

    /**
     *
     */
    public void showMetric2Button() {
        Platform.runLater(dashboardController::showMetric2Button);
    }

    /**
     *
     */
    public void hideFields() {
        dashboardController.hideBounceFields();
    }
    /**
     *
     */
    public void showBounceFields() {
        dashboardController.showBounceFields();
    }
    /**
     *
     */
    public int getBounceTime() {
        return dashboardController.getBounceTime();
    }
    /**
     *
     */
    public int getPagesViewed() {
        return dashboardController.getPagesViewed();
    }
    /**
     *
     */
    public void setSeriesName(int viewIndex, String name, Graph graph, int index) {
        Platform.runLater(() -> dashboardController.setSeriesName(viewIndex, name, graph, index));
    }
    /**
     *
     */
    public Stage getStage() {
        return stage;
    }
    /**
     *
     */
    public Scene getLoginScene() {
        return loginScene;
    }
    /**
     *
     */
    public Scene getDashboardScene() {
        return dashboardScene;
    }
    /**
     *
     */
    public Scene getFileUploadScene() {
        return fileUploadScene;
    }
    /**
     *
     */
    public Scene getSettingsScene() {
        return settingsScene;
    }
    /**
     *
     */
    public Scene getManualScene() {
        return manualScene;
    }
    /**
     *
     */
    public Scene getUserManagerScene() {
        return userManagerScene;
    }

    /**
     *
     * @param graph
     * @param viewIndex
     */
    public void refreshCharts(Graph graph, int viewIndex) {
        Platform.runLater(() -> dashboardController.refreshCharts(graph, viewIndex));
    }

    /**
     * Updates bar chart with data passed into function
     *
     * @param data TreeMap containing data. String represents Date, Integer represents
     *             numerical field going on to Y Axis.
     */
    public void updateChartAsync(int viewIndex, TreeMap<String, ? extends Number> data, Graph graph, int graphIndex) {
        Platform.runLater(() -> updateChart(viewIndex, data, graph, graphIndex));
    }

    /**
     *
     * @param viewIndex
     * @param data
     * @param graph
     * @param graphIndex
     */
    public void updateChart(int viewIndex, TreeMap<String, ? extends Number> data, Graph graph, int graphIndex) {
        dashboardController.updateChart(viewIndex, data, graph, graphIndex);
    }

    /**
     *
     * @param viewIndex
     * @param show
     */
    public void showGraphLoading(int viewIndex, boolean show) {
        dashboardController.showLoading(viewIndex, show);
    }

    /**
     *
     * @param show
     */
    public void showUploadLoading(boolean show) {
        uploadController.showLoading(show);
    }

    /**
     *
     * @param viewIndex
     * @param title
     */
    public void bindTitle(int viewIndex, SimpleStringProperty title) {
        dashboardController.bindTitle(viewIndex, title);
    }

    /**
     *
     * @return
     */
    public boolean isLineChart1Visible() {
        return dashboardController.isLineChart1Visible();
    }

    /**
     *
     * @return
     */
    public boolean isLineChart2Visible() {
        return dashboardController.isLineChart2Visible();
    }

    /**
     *
     * @param listener
     */
    public void setOnFilterApply(FilterListener listener) {
        dashboardController.setFilterListener(listener);
    }


    /**
     * Restrict certain actions (view user operations, manage users) based on the current user's role
     */
    public void restrictActions() {
        if (Objects.equals(usersDatabase.getRole(Context.getCurrentUser()), "Admin")) {
            dashboardController.getOperationsButton().setVisible(true);
            dashboardController.getUserManagerButton().setVisible(true);
        } else if (Objects.equals(usersDatabase.getRole(Context.getCurrentUser()), "Editor")) {
            dashboardController.getOperationsButton().setVisible(true);
        }
    }

    /**
     *
     * @return
     */
    public ManualController getManualController() {
        return manualController;
    }
}
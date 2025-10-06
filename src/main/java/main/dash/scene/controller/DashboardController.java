package main.dash.scene.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.transform.Transform;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import javafx.util.Duration;
import javax.imageio.ImageIO;

import main.dash.common.AlarmUtils;
import main.dash.common.Context;
import main.dash.common.OperationLogger;
import main.dash.data.DataController;
import main.dash.common.Operation;
import main.dash.data.DataModel;
import main.dash.enums.Graph;
import main.dash.enums.Metric;
import main.dash.enums.SceneName;
import main.dash.enums.TimeGranularity;
import main.dash.event.MetricListener;
import main.dash.event.SceneListener;
import main.dash.event.TimeGranularityListener;
import main.dash.event.FilterListener;
import javafx.embed.swing.SwingFXUtils;

import main.dash.scene.DataView;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.controlsfx.control.CheckComboBox;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.property.TextAlignment;


import static main.dash.enums.Metric.*;

public class DashboardController {

    @FXML
    private StackPane pane1, pane2;

    @FXML
    private Text chart1Text, chart1OverlayText, chart2Text, chart3Text;

    @FXML
    private Text overlayText;

    @FXML
    private Text startDateText, startDateText1, startDateText2, endDateText, endDateText1, endDateText2;

    @FXML
    private Text genderText1, ageText1, incomeText1,contextText1, genderText2, ageText2, incomeText2,contextText2;

    @FXML
    private LineChart<String, Number> lineChart, lineChart1, lineChart2;

    @FXML
    private Parent bounceFields;

    @FXML
    private BarChart<String, Number> barChart, barChart1, barChart2;

    @FXML
    private CategoryAxis lineXAxis, barXAxis, lineXAxis1, barXAxis1, lineXAxis2, barXAxis2;
    @FXML
    private NumberAxis lineYAxis, barYAxis, lineYAxis1, barYAxis1, lineYAxis2, barYAxis2;

    @FXML
    private MenuItem settingsButton, uploadButton, manualButton, userManagerItem;

		@FXML
		private Button dashboardReturnButton, applyDateFilterButton, applyDateFilterButton1, applyDateFilterButton2;

    @FXML
    private Menu operationsButton, userManagerButton;

    @FXML
    private Region spacer;

    @FXML
    private BounceFields bounceFieldsController;
    private MetricListener metricListener;

    private DataView view;

    @FXML
    private DatePicker startDatePicker, startDatePicker1, startDatePicker2, endDatePicker, endDatePicker1, endDatePicker2;

    @FXML
    private CheckComboBox<String> genderComboBox, ageComboBox, incomeComboBox, contextComboBox;
    @FXML
    private CheckComboBox<String> genderComboBox1, ageComboBox1, incomeComboBox1, contextComboBox1;
    @FXML
    private CheckComboBox<String> genderComboBox2, ageComboBox2, incomeComboBox2, contextComboBox2;

    @FXML
    private ComboBox<String> timeButton, metric2Button, next1Button, next2Button;

    @FXML
    private Parent root;
    @FXML
    private ImageView loadingIcon1, loadingIcon2, loadingIcon3;

    private FilterListener filterListener;
    private TimeGranularityListener timeGranularityListener;

    public DashboardController() {

    }

    @FXML
    private void initialize() {
        initializeGraph(barChart, 2);
        initializeGraph(lineChart, 2);
        initializeGraph(barChart1, 1);
        initializeGraph(lineChart1, 1);
        initializeGraph(barChart2, 1);
        initializeGraph(lineChart2, 1);

        loadingIcon1.setImage(new Image(Objects.requireNonNull(getClass().getResource("/loading.gif")).toExternalForm()));
        loadingIcon2.setImage(new Image(Objects.requireNonNull(getClass().getResource("/loading.gif")).toExternalForm()));
        loadingIcon3.setImage(new Image(Objects.requireNonNull(getClass().getResource("/loading.gif")).toExternalForm()));

        HBox.setHgrow(spacer, Priority.SOMETIMES);

        // Set up the date pickers, initialising them to have the default values based on the dates used in the campaigns
        LocalDate[] dates = DataModel.getEarliestAndLatestDates();
        if (dates != null) {
            LocalDate earliestDate = dates[0];
            LocalDate latestDate = dates[1];

            setDatePickerCellFactory(startDatePicker, earliestDate, latestDate);
            setDatePickerCellFactory(startDatePicker1, earliestDate, latestDate);
            setDatePickerCellFactory(startDatePicker2, earliestDate, latestDate);
            setDatePickerCellFactory(endDatePicker, earliestDate, latestDate);
            setDatePickerCellFactory(endDatePicker1, earliestDate, latestDate);
            setDatePickerCellFactory(endDatePicker2, earliestDate, latestDate);

            startDatePicker.setValue(earliestDate);
            startDatePicker1.setValue(earliestDate);
            startDatePicker2.setValue(earliestDate);
            endDatePicker.setValue(latestDate);
            endDatePicker1.setValue(latestDate);
            endDatePicker2.setValue(latestDate);
        }

        hideBounceFields();
        hideMetric2Button();
    }

    private void initializeGraph(XYChart<String, Number> chart, int count){
        for(int i = 0; i < count; i++) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            chart.getData().add(series);

        }

        chart.setCursor(Cursor.CROSSHAIR);
    }

    public void setTotalText(int graph, Metric metric){
        Text chartText = null;
        switch (graph) {
            case 0 -> chartText = chart1Text;
            case 1 -> chartText = chart1OverlayText;
            case 2 -> chartText = chart2Text;
            case 3 -> chartText = chart3Text;
        }

        if(chartText == null) return;

        switch (metric){
            case UNIQUES -> bindText(chartText, "Number of unique users: ", DataController.getUniqueUserCountProp().asString(), null);
            case CLICKS -> bindText(chartText, "Number of clicks: ", DataController.getNumberOfClicksProp().asString(), null);
            case AVG_CLICKS -> bindText(chartText, "Average clicks per impression: ", DataController.getAvgClicksPerImpressionProp().asString(), null);
            case IMPRESSIONS -> bindText(chartText, "Number of impressions: ", DataController.getNumberOfImpressionsProp().asString(), null);
            case COST -> bindText(chartText, "Total cost: ", DataController.getTotalCostProp().asString(), null);
            case CONVERSIONS -> bindText(chartText, "Number of conversions: ", DataController.getNumberOfConversionsProp().asString(), null);
            case CPM -> bindText(chartText, "CPM (Cost per 1000 Impressions): ", DataController.getCpmProp().asString(), null);
            case BOUNCES -> bindText(chartText, "Total number of bounces: ", DataController.getTotalBouncesProp().asString(), null);
            case COST_PER_CLICK -> bindText(chartText, "CPC (Average amount of money spent per click): ", DataController.getCostPerClickProp().asString(), null);
            case BOUNCE_RATE -> bindText(chartText, "Bounce Rate: ", DataController.getBounceRateProp().asString(), null);
            case COST_PER_CONVERSION -> bindText(chartText, "Cost Per Conversion: ", DataController.getCostPerConversionProp().asString(), null);
            case COST_DISTRIBUTION -> bindText(chartText, "Cost Distribution: ", DataController.getCostDistributionProp().asString(), null);
        }

    }

    public void handleEvents(SceneListener sceneListener, MetricListener metricListener, TimeGranularityListener timeGranularityListener) {
        this.metricListener = metricListener;
        this.timeGranularityListener = timeGranularityListener;
        dashboardReturnButton.setOnAction(e -> {
            OperationLogger.recordAction("Return  to Login Scene");
            sceneListener.sceneChanged(SceneName.LOGIN);
        });
        settingsButton.setOnAction(e -> {
            OperationLogger.recordAction("Click Help->Settings");
            sceneListener.sceneChanged(SceneName.SETTINGS);
        });
        manualButton.setOnAction(e -> {
            OperationLogger.recordAction("Click Help->Manual");
            sceneListener.sceneChanged(SceneName.MANUAL);
            view.getManualController().setReturnScene(SceneName.DASHBOARD);
        });
        uploadButton.setOnAction(e -> {
            OperationLogger.recordAction("Click File->IMPORT");
            sceneListener.sceneChanged(SceneName.FILE_UPLOAD);
        });
        userManagerItem.setOnAction(e -> {
            OperationLogger.recordAction("User Manager");
            sceneListener.sceneChanged(SceneName.USER_MANAGER);
        });

    applyDateFilterButton.setOnAction(
        e -> {
            if (startDatePicker.getValue().isBefore(endDatePicker.getValue())) {
                OperationLogger.recordAction("Date filters applied to Graph One");
                applyFilters(0);
            } else {
                AlarmUtils.alarm("Start date must be before end date!");
            }
        });
        applyDateFilterButton1.setOnAction(e -> {
            if (startDatePicker1.getValue().isBefore(endDatePicker1.getValue())) {
                OperationLogger.recordAction("Date filters applied to Graph Two");
                applyFilters(1);
            } else {
                AlarmUtils.alarm("Start date must be before end date!");
            }
        });
        applyDateFilterButton2.setOnAction(e -> {
            if (startDatePicker2.getValue().isBefore(endDatePicker2.getValue())) {
                OperationLogger.recordAction("Date filters applied to Graph Three");
                applyFilters(2);
            } else {
                AlarmUtils.alarm("Start date must be before end date!");
            }
        });

        setComboBoxListener(genderComboBox, 0);
        setComboBoxListener(ageComboBox, 0);
        setComboBoxListener(incomeComboBox, 0);
        setComboBoxListener(contextComboBox, 0);

        setComboBoxListener(genderComboBox1, 1);
        setComboBoxListener(ageComboBox1, 1);
        setComboBoxListener(incomeComboBox1, 1);
        setComboBoxListener(contextComboBox1, 1);

        setComboBoxListener(genderComboBox2, 2);
        setComboBoxListener(ageComboBox2, 2);
        setComboBoxListener(incomeComboBox2, 2);
        setComboBoxListener(contextComboBox2, 2);
    }

    private void setComboBoxListener(CheckComboBox<String> comboBox, int viewIndex){
	    ListChangeListener<String> comboBoxListener = change -> {
		    Platform.runLater(() -> {
					System.out.println("ComboBox changed");

					boolean updated = false;
			    while(change.next()){
				    if(change.wasAdded() || change.wasRemoved()) updated = true;
			    }

					if(updated) applyFilters(viewIndex);
		    });
	    };

			comboBox.getCheckModel().getCheckedItems().addListener(comboBoxListener);
    }

    public void refreshCharts(Graph graph, int viewIndex) {
        if (graph == Graph.HISTOGRAM) {
            switch (viewIndex) {
                case 0 -> lineChart.setVisible(false);
                case 1 -> lineChart1.setVisible(false);
                case 2 -> lineChart2.setVisible(false);
            }
        } else {
            switch (viewIndex) {
                case 0 -> barChart.setVisible(false);
                case 1 -> barChart1.setVisible(false);
                case 2 -> barChart2.setVisible(false);
            }
        }
    }

    /**
     * Passes the correct chart into the other updateChart function
     *
     * @param viewIndex Index indicating which of the 3 charts is to be updated
     * @param data      Data to be displayed on the chart
     * @param graph     Type of chart displayed
     * @param chart     Index indicating whether the data passed is to be displayed or overlain on a graph
     */
    public void updateChart(int viewIndex, TreeMap<String, ? extends Number> data, Graph graph, int chart) {
        if (chart == 0) {
            switch (viewIndex) {
                case (0) -> this.updateChart(data, graph, chart, this.barChart, this.lineChart);
                case (1) -> this.updateChart(data, graph, chart, this.barChart1, this.lineChart1);
                case (2) -> this.updateChart(data, graph, chart, this.barChart2, this.lineChart2);
            }
        } else if (chart == 1) {
            if (viewIndex == 0) {
                this.updateChart(data, graph, chart, this.barChart, this.lineChart);
            } else if (viewIndex == 1) {
                this.updateChart(data, graph, chart, this.barChart1, this.lineChart1);
            } else if (viewIndex == 2) {
                this.updateChart(data, graph, chart, this.barChart2, this.lineChart2);
            }
        }
    }

    /**
     * Function to add the passed data to the correct series on the passed graph
     *
     * @param data      Data to be added onto the graph's series
     * @param graph     Type of graph to be displayed
     * @param chart     Index indicating which series of a graph the data should be added to
     * @param barChart  Bar chart which may have data added, depending on graph's value
     * @param lineChart Line chart which may have data added to it, depending on graph's value
     */
    public void updateChart(TreeMap<String, ? extends Number> data, Graph graph, int chart, BarChart<String, Number> barChart, LineChart<String, Number> lineChart) {
        XYChart.Series<String, Number> series = null;


        // Do not delete this, it stops a visual error caused when switching metrics from occurring
        if (chart == 0) {
            this.lineChart.getData().remove(1);
            this.lineChart.getData().add(new XYChart.Series<String, Number>());
        }

        String xAxis;
        String yAxis;

        // Get the correct series assigned to that chart
        switch (graph) {
            case LINE -> {
                lineChart.setVisible(true);
                xAxis = lineChart.getXAxis().getLabel();
                yAxis = lineChart.getYAxis().getLabel();
                series = lineChart.getData().get(chart);
            }
            case HISTOGRAM -> {
                barChart.setVisible(true);
                xAxis = barChart.getXAxis().getLabel();
                yAxis = barChart.getYAxis().getLabel();
                series = barChart.getData().get(chart);
            }

            default -> throw new IllegalArgumentException("Invalid graph type");
        }

        // Clear existing data in the series
        getOverlainSeries().getData().clear();
        series.getData().clear();
        // Add passed in data to the series and setup a hover event
        for (var entry : data.entrySet()) {
            XYChart.Data<String, Number> point = new XYChart.Data<>(entry.getKey(), entry.getValue());

            point.setNode(new StackPane());
            String xAxisSeries = xAxis.split("/")[chart];
            String yAxisSeries = yAxis.split("/")[chart];
            double multiplier = Math.pow(10, DataModel.getDP());
            double yValue = Math.round(entry.getValue().doubleValue() * multiplier) / multiplier;
            String valueText = xAxisSeries + ": " + entry.getKey() + "\n" +
                yAxisSeries + ": " + yValue;
            Tooltip tooltip = new Tooltip(valueText);
            tooltip.setShowDelay(Duration.millis(200));
            tooltip.setHideDelay(Duration.millis(200));
            Tooltip.install(point.getNode(), tooltip);

            series.getData().add(point);
        }
    }

    /**
     * Binds a text object to a string property
     *
     * @param prefix     Initial contents of the text object
     * @param bindString String property to bind to
     * @param css        CSS class to apply to the text object
     */
    protected void bindText(Text text, String prefix, StringBinding bindString, String css) {
        text.textProperty().bind(Bindings.concat(prefix, bindString));
        text.getStyleClass().add(css);
    }

    public void hideMetric2Button() {
        overlayText.setVisible(false);
        metric2Button.setVisible(false);
    }

    public void showMetric2Button() {
        overlayText.setVisible(true);
        metric2Button.setVisible(true);
    }

    public void hideBounceFields() {
        bounceFields.setVisible(false);
    }

    public void showBounceFields() {
        bounceFields.setVisible(true);
    }

    public int getBounceTime() {
        return bounceFieldsController.getBounceTime();
    }

    public int getPagesViewed() {
        return bounceFieldsController.getPagesViewed();
    }

    public XYChart.Series<String, Number> getOverlainSeries() {
        return lineChart.getData().get(1);
    }

    public Menu getOperationsButton() {
        return operationsButton;
    }

    public MenuItem getUserManagerButton() {
        return userManagerButton;
    }

    public void setSeriesName(int viewIndex, String name, Graph graph, int index) {
        switch (graph) {
            case LINE -> {
                if (index == 1) {
                    lineChart.getData().get(index).setName(name);
                } else {
                    if (viewIndex == 0) {
                        lineChart.getData().get(index).setName(name);
                    } else if (viewIndex == 1) {
                        lineChart1.getData().get(index).setName(name);
                    } else if (viewIndex == 2) {
                        lineChart2.getData().get(index).setName(name);

                    }
                }

            }
            case HISTOGRAM -> {
                if (index == 1) {
                    barChart.getData().get(index).setName(name);
                } else {
                    if (viewIndex == 0) {
                        barChart.getData().get(index).setName(name);
                    } else if (viewIndex == 1) {
                        barChart1.getData().get(index).setName(name);
                    } else if (viewIndex == 2) {
                        barChart2.getData().get(index).setName(name);
                    }
                }

            }
        }
    }

    @FXML
    private void exit() {
        view.exit(null);
    }

    @FXML
    private void exportPDF() {
        PDDocument document = new PDDocument();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("src"));

        ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.PDF", "*.pdf");
        fileChooser.getExtensionFilters().add(pdfFilter);

        File directory = fileChooser.showSaveDialog(view.getStage());
        if(directory == null) return;
        String path = directory.getAbsolutePath();

        if(lineChart.isVisible()) document = exportChartPDF(path, lineChart, document);
        if(barChart.isVisible()) document = exportChartPDF(path, barChart, document);
        if(lineChart1.isVisible()) document = exportChartPDF(path, lineChart1, document);
        if(barChart1.isVisible()) document = exportChartPDF(path, barChart1, document);
        if(lineChart2.isVisible()) document = exportChartPDF(path, lineChart2, document);
        if(barChart2.isVisible()) document = exportChartPDF(path, barChart2, document);

        try {
            document.close();
        } catch (IOException e) {
            AlarmUtils.alarm("export pdf error");
        }
        OperationLogger.recordAction("Click File->Export->PDF,path:" + path);

        AlarmUtils.alarm("The chart has been exported as a PDF to path: " + path);
    }

    private PDDocument exportChartPDF(String path, Chart chart, PDDocument document) {

        PDPage page = new PDPage();
        document.addPage(page);

        SnapshotParameters sp = new SnapshotParameters();
        Transform transform = Transform.scale(1.4, 1.4); // increase for larger image
        sp.setTransform(transform);

        WritableImage graphSnapshot = chart.snapshot(sp, null);
        //WritableImage totalSnapshop
        File imageFile = new File(path + ".png");

        try {
            ImageIO.write(SwingFXUtils.fromFXImage(graphSnapshot, null), "png", imageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            PDImageXObject pdImage = PDImageXObject.createFromFile(path + ".png", document);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.drawImage(pdImage, 0, 0); // Adjust position as needed
            contentStream.close();

            document.save(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Clean up
        imageFile.delete();

        return document;
    }

    @FXML
    private void exportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("src"));

        ExtensionFilter csvFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.CSV", "*.csv");
        fileChooser.getExtensionFilters().add(csvFilter);

        File directory = fileChooser.showSaveDialog(view.getStage());
        String path = directory.getAbsolutePath();
        if (path!=null|| !path.isEmpty()) {
            try {
                exportData(path);
            } catch (Exception e) {
                AlarmUtils.alarm("export csv error");
            }
        }
        OperationLogger.recordAction("Click File->Export->CSV,path:" + path);
        AlarmUtils.alarm("The chart has been exported as a CSV to path: " + path);
    }

    private void exportData(String path) {
        List<String> lines = new ArrayList<>();
        lines.add("Graph, Series, Type, Metric, Time, Gender, Age, Income X, Y");

        String filters = DataController.getTimeGranularity() + "," + DataController.getSelectedGenders()
                + "," + DataController.getSelectedAges() + "," + DataController.getSelectedIncomes()+","+DataController.getSelectedContexts();

        lineChart.getData().get(0).getData().forEach(data ->
                lines.add("0, 0, LINE, " + DataController.getSelectedMetric()[0] +
                        "," + filters + "," + data.getXValue() + "," + data.getYValue()));

        lineChart.getData().get(1).getData().forEach(data ->
                lines.add("0, 1, LINE, " + DataController.getSelectedMetric()[1] +
                        "," + filters + "," + data.getXValue() + "," + data.getYValue()));

        barChart.getData().get(0).getData().forEach(data ->
                lines.add("0, 0, HISTOGRAM, " + DataController.getSelectedMetric()[0] +
                        "," + filters + "," + data.getXValue() + "," + data.getYValue()));

        lineChart1.getData().get(0).getData().forEach(data ->
                lines.add("1, 0, LINE, " + DataController.getSelectedMetric()[2] +
                        "," + filters + "," + data.getXValue() + "," + data.getYValue()));

        barChart1.getData().get(0).getData().forEach(data ->
                lines.add("1, 0, HISTOGRAM, " + DataController.getSelectedMetric()[2] +
                        "," + filters + "," + data.getXValue() + "," + data.getYValue()));

        lineChart2.getData().get(0).getData().forEach(data ->
                lines.add("2, 0, LINE, " + DataController.getSelectedMetric()[3] +
                        "," + filters + "," + data.getXValue() + "," + data.getYValue()));

        barChart2.getData().get(0).getData().forEach(data ->
                lines.add("2, 0, HISTOGRAM, " + DataController.getSelectedMetric()[3] +
                        "," + filters + "," + data.getXValue() + "," + data.getYValue()));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }

            System.out.println("File has been written successfully.");
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file.");
            e.printStackTrace();
        }
    }

    public void showLoading(int viewIndex, boolean show){
        Platform.runLater(() -> {
            switch (viewIndex){
                case 0 -> loadingIcon1.setVisible(show);
                case 1 -> loadingIcon2.setVisible(show);
                case 2 -> loadingIcon3.setVisible(show);
            }
        });
    }

    public void setLineXAxis(String label, int viewIndex) {
        switch (viewIndex) {
            case 0 -> lineXAxis.setLabel(label);
            case 1 -> lineXAxis1.setLabel(label);
            case 2 -> lineXAxis2.setLabel(label);
        }
    }

    public void setLineYAxis(String label, int viewIndex) {
        switch (viewIndex) {
            case 0 -> lineYAxis.setLabel(label);
            case 1 -> lineYAxis1.setLabel(label);
            case 2 -> lineYAxis2.setLabel(label);
        }
    }

    public void setBarXAxis(String label, int viewIndex) {
        switch (viewIndex) {
            case 0 -> barXAxis.setLabel(label);
            case 1 -> barXAxis1.setLabel(label);
            case 2 -> barXAxis2.setLabel(label);
        }
    }

    public void setBarYAxis(String label, int viewIndex) {
        switch (viewIndex) {
            case 0 -> barYAxis.setLabel(label);
            case 1 -> barYAxis1.setLabel(label);
            case 2 -> barYAxis2.setLabel(label);
        }
    }

    public BounceFields getBounceFieldsController() {
        return bounceFieldsController;
    }

    public boolean isLineChart1Visible() {
        return pane1.isVisible();
    }

    public boolean isLineChart2Visible() {
        return pane2.isVisible();
    }

    private void setFontSize() {
        view.bindFont(root, 1);

        float chartFontSize = 1f;
        view.bindFont(lineChart, chartFontSize);
        view.bindFont(barChart, chartFontSize);
        view.bindFont(lineChart1, chartFontSize);
        view.bindFont(barChart1, chartFontSize);
        view.bindFont(lineChart2, chartFontSize);
        view.bindFont(barChart2, chartFontSize);
    }

    public void setView(DataView view) {
        this.view = view;
        setFontSize();
    }

    public void bindTitle(int viewIndex, SimpleStringProperty title) {
        switch (viewIndex) {
            case 0:
                lineChart.titleProperty().bind(title);
                barChart.titleProperty().bind(title);
                break;
            case 1:
                lineChart1.titleProperty().bind(title);
                barChart1.titleProperty().bind(title);
                break;
            case 2:
                lineChart2.titleProperty().bind(title);
                barChart2.titleProperty().bind(title);
                break;
        }
    }

    public void setFilterListener(FilterListener filterListener) {
        this.filterListener = filterListener;
    }

  @FXML
  private void applyFilters(int view) {
    if (filterListener == null) return;

		System.out.println("Box checked");

    String[] genderBox;
    String[] ageBox;
    String[] incomeBox;
    String[] contextBox;
    LocalDate startDate;
    LocalDate endDate;

    switch (view) {
      case 0:
        genderBox = comboBoxToArray(genderComboBox);
        ageBox = comboBoxToArray(ageComboBox);
        incomeBox = comboBoxToArray(incomeComboBox);
        contextBox = comboBoxToArray(contextComboBox);
        startDate = startDatePicker.getValue();
        endDate = endDatePicker.getValue();
        break;

      case 1:
        genderBox = comboBoxToArray(genderComboBox1);
        ageBox = comboBoxToArray(ageComboBox1);
        incomeBox = comboBoxToArray(incomeComboBox1);
        contextBox = comboBoxToArray(contextComboBox1);
        startDate = startDatePicker1.getValue();
        endDate = endDatePicker1.getValue();
        break;

      case 2:
        genderBox = comboBoxToArray(genderComboBox2);
        ageBox = comboBoxToArray(ageComboBox2);
        incomeBox = comboBoxToArray(incomeComboBox2);
        contextBox = comboBoxToArray(contextComboBox2);
        startDate = startDatePicker2.getValue();
        endDate = endDatePicker2.getValue();
        break;

      default:
        System.out.println("Invalid view index");
        return;
    }

    filterListener.filterApplied(view, genderBox, ageBox, incomeBox,contextBox, startDate, endDate);
	}

    private String[] comboBoxToArray(CheckComboBox<String> comboBox){
        Object[] comboBoxObj = comboBox.getCheckModel().getCheckedItems().toArray();
        return Arrays.copyOf(comboBoxObj, comboBoxObj.length, String[].class);
    }

    @FXML
    private void applyMetric(ActionEvent actionEvent) {
        ComboBox<String> comboBox = (ComboBox<String>) actionEvent.getSource();

        String indexes = comboBox.getUserData().toString();
        int view = Integer.parseInt(indexes.split(",")[0]);
        int chart = Integer.parseInt(indexes.split(",")[1]);

        OperationLogger.recordSelect("SELECT "+comboBox.getValue());

        Metric metric;
        switch(comboBox.getValue()){
            case "Number of clicks" -> metric = CLICKS;
            case "Number of unique users" -> metric = UNIQUES;
            case "Average clicks per impression" -> metric = AVG_CLICKS;
            case "Number of impressions" -> metric = IMPRESSIONS;
            case "Total cost" -> metric = COST;
            case "Number of conversions" -> metric = CONVERSIONS;
            case "Cost per 1000 impressions" -> metric = CPM;
            case "Number of bounces" -> metric = BOUNCES;
            case "Cost per click" -> metric = COST_PER_CLICK;
            case "Rate of bounces" -> metric = BOUNCE_RATE;
            case "Cost per conversion" -> metric = COST_PER_CONVERSION;
            case "Distribution of clicks" -> metric = COST_DISTRIBUTION;
	          default -> {return;}
        }

        if(view == 1){
            startDateText1.setVisible(true);
            startDatePicker1.setVisible(true);
            endDateText1.setVisible(true);
            endDatePicker1.setVisible(true);
            applyDateFilterButton1.setVisible(true);
            genderComboBox1.setVisible(true);
            ageComboBox1.setVisible(true);
            incomeComboBox1.setVisible(true);
            contextComboBox1.setVisible(true);
            genderText1.setVisible(true);
            ageText1.setVisible(true);
            incomeText1.setVisible(true);
            contextText1.setVisible(true);
            pane1.setVisible(true);
            pane1.setManaged(true);
        }
        if(view == 2){
            startDateText2.setVisible(true);
            startDatePicker2.setVisible(true);
            endDateText2.setVisible(true);
            endDatePicker2.setVisible(true);
            applyDateFilterButton2.setVisible(true);
            genderComboBox2.setVisible(true);
            ageComboBox2.setVisible(true);
            incomeComboBox2.setVisible(true);
            contextComboBox2.setVisible(true);
            genderText2.setVisible(true);
            ageText2.setVisible(true);
            incomeText2.setVisible(true);
            contextText2.setVisible(true);
            pane2.setVisible(true);
            pane2.setManaged(true);
        }

        metricListener.metricChanged(view, metric, chart);

    }

    public void applyTime() {
        TimeGranularity timeGranularity = TimeGranularity.valueOf(timeButton.getValue().toUpperCase());
        OperationLogger.recordAction("SELECT "+timeGranularity);
        timeGranularityListener.timeGranularityChanged(timeGranularity);
    }

    /**
     * the action of click export log
     * output a csv file into select path
     * @param actionEvent
     */
    @FXML
    public void exportOperationCSV(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(javax.swing.filechooser.FileSystemView.getFileSystemView().getHomeDirectory());

        ExtensionFilter csvFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.CSV", "*.csv");
        fileChooser.getExtensionFilters().add(csvFilter);

        File directory = fileChooser.showSaveDialog(view.getStage());
        String path = directory.getAbsolutePath();
        OperationLogger.recordAction("Click Operation->EXPORT CSV,path:" + path);
        if (path != null && !path.isEmpty()) {
            List<Operation> operationLogs = Context.getOperationLogs();
            List<String> lines = new ArrayList<>();
            lines.add("Index, User, Type, content, Time, Remark");
            StringBuilder builder = new StringBuilder();
            int index = 1;
            char delimiter = ',';
            DateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (Operation operationLog : operationLogs) {
                builder.append(index);
                builder.append(delimiter);
                builder.append(operationLog.getName());
                builder.append(delimiter);
                builder.append(operationLog.getOperationEnum().getType());
                builder.append(delimiter);
                builder.append("\"");
                builder.append(operationLog.getContent());
                builder.append("\"");
                builder.append(delimiter);
                builder.append(dateTimeFormatter.format(operationLog.getOperatingTime()));
                builder.append(delimiter);
                builder.append(operationLog.getRemark() == null ? "" : operationLog.getRemark());
                lines.add(builder.toString());
                builder.setLength(0);
                index++;
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
                writer.flush();
            } catch (IOException e) {
                AlarmUtils.alarm("export csv error");
            }
        }
    }
    /**
     * the action of click export log
     * output a pdf file into select path
     * @param actionEvent
     */
    @FXML
    public void exportOperationPDF(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(javax.swing.filechooser.FileSystemView.getFileSystemView().getHomeDirectory());

        FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.PDF", "*.pdf");
        fileChooser.getExtensionFilters().add(pdfFilter);

        File file = fileChooser.showSaveDialog(view.getStage());
        if (file == null) return;

        String path = file.getAbsolutePath();
        OperationLogger.recordAction("Click Operation->EXPORT PDF,path:" + path);

        List<Operation> operationLogs = Context.getOperationLogs();
        DateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            PdfWriter writer = new PdfWriter(path);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Title
            document.add(new Paragraph("User Operations Log")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            // Table: 6 columns
            Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2, 2, 3, 2, 2}))
                    .useAllAvailableWidth();

            // Headers
            table.addHeaderCell("Index");
            table.addHeaderCell("User");
            table.addHeaderCell("Type");
            table.addHeaderCell("Content");
            table.addHeaderCell("Time");
            table.addHeaderCell("Remark");

            // Data rows
            int index = 1;
            for (Operation operationLog : operationLogs) {
                table.addCell(String.valueOf(index++));
                table.addCell(operationLog.getName());
                table.addCell(operationLog.getOperationEnum().getType());
                table.addCell(operationLog.getContent());
                table.addCell(dateTimeFormatter.format(operationLog.getOperatingTime()));
                table.addCell(operationLog.getRemark() == null ? "" : operationLog.getRemark());
            }

            document.add(table);
            document.close();

        } catch (IOException e) {
            AlarmUtils.alarm("export pdf error");
        }
    }

    private void setDatePickerCellFactory(DatePicker datePicker, LocalDate campaignStartDate, LocalDate campaignEndDate) {
        datePicker.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                boolean availableDate = item.isBefore(campaignStartDate) || item.isAfter(campaignEndDate);
                setDisable(availableDate);
                if (availableDate) {setStyle("-fx-background-color: transparent; -fx-text-fill: transparent;");}
                else setStyle("");
            }
        });
    }
}

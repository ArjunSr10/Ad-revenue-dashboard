package main.dash.data;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.TreeMap;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.stage.Stage;
import main.dash.database.metrics.clickLogDatabase;
import main.dash.database.metrics.impressionLogDatabase;
import main.dash.database.metrics.serverLogDatabase;
import main.dash.enums.FileType;
import main.dash.enums.Graph;
import main.dash.scene.DataView;
import main.dash.enums.SceneName;
import main.dash.enums.Metric;
import main.dash.enums.TimeGranularity;


public class DataController {
    /**
     * Represents the view component of MVC pattern
     */
    private DataView view;
    /**
     * Represents current time granularity used in the charts.
     */
    private static TimeGranularity currentTimeGranularity;
    /**
     * Represents currently overlain metric on the chart
     */
    private static Metric[] currentMetrics;

    //Properties to be bound to the view
    protected static SimpleIntegerProperty uniqueUserCountProp = new SimpleIntegerProperty();
    protected static SimpleIntegerProperty numberOfClicksProp = new SimpleIntegerProperty();
    protected static SimpleDoubleProperty avgClicksPerImpressionProp = new SimpleDoubleProperty();
    protected static SimpleIntegerProperty numberOfImpressionsProp = new SimpleIntegerProperty();
    protected static SimpleDoubleProperty totalCostProp = new SimpleDoubleProperty();
    protected static SimpleIntegerProperty numberOfConversionsProp = new SimpleIntegerProperty();
    protected static SimpleDoubleProperty cpmProp = new SimpleDoubleProperty();
    protected static SimpleDoubleProperty totalBouncesProp = new SimpleDoubleProperty();
    protected static SimpleDoubleProperty costPerClickProp = new SimpleDoubleProperty();
    protected static SimpleDoubleProperty bounceRateProp = new SimpleDoubleProperty();
    protected static SimpleDoubleProperty costPerConversionProp = new SimpleDoubleProperty();
    protected static SimpleDoubleProperty costDistributionProp = new SimpleDoubleProperty();

    private Stage stage;

    private String xAxis, yAxis;

    private static String[] selectedGenders = new String[0];
    private static String[] selectedAges = new String[0];
    private static String[] selectedIncomes = new String[0];
    private static String[] selectedContexts = new String[0];
    private static LocalDate[] selectedDates = new LocalDate[2];

    private final SimpleStringProperty[] chartTitles = new SimpleStringProperty[3];

    public DataController(Stage stage, DataView view) {
        this.view = view;
        this.stage = stage;

        currentTimeGranularity = TimeGranularity.DAY;
        currentMetrics = new Metric[4]; // Array containing the current metrics used.
    // Metric[0] is the metric displayed on the first graph
    // Metric[1] is the metric overlain on the first graph
    // Metric[2] is the metric displayed on the second graph
    // Metric[3] is the metric displayed on the third graph

        chartTitles[0] = new SimpleStringProperty("No data");
        chartTitles[1] = new SimpleStringProperty("No data");
        chartTitles[2] = new SimpleStringProperty("No data");
        view.bindTitle(0, chartTitles[0]);
        view.bindTitle(1, chartTitles[1]);
        view.bindTitle(2, chartTitles[2]);

        view.setOnSceneChange(this::handleSceneChange);
        view.setOnMetricApply(this::handleFilters);
        view.setOnTimeGranularityApply(this::handleTimeGranularity);
        view.setOnFieldUpdate(this::handleFieldUpdate);
        view.setOnFileUpload(this::handleFileUpload);
        view.setOnFilterApply(this::setFilters);
    }

    public static TimeGranularity getTimeGranularity() {
        return currentTimeGranularity;
    }

    public static Metric[] getSelectedMetric() {
        return currentMetrics;
    }

    private void handleFieldUpdate(ObservableValue<? extends String> observableValue, String s, String s1) {
        for(int i = 0; i < currentMetrics.length; i++) {
            if(currentMetrics[i] == null || !(currentMetrics[i] == Metric.BOUNCES || currentMetrics[i] == Metric.BOUNCE_RATE)) continue;

            int viewIndex = switch(i) {
                case 0, 1 -> 0;
	              case 2 -> 1;
                case 3 -> 2;
	              default -> throw new IllegalStateException("Unexpected value: " + i);
            };

            loadChartData(viewIndex);
        }
    }

    private void handleSceneChange(SceneName sceneName) {
        switch (sceneName) {
            case LOGIN -> stage.setScene(view.getLoginScene());
            case DASHBOARD -> stage.setScene(view.getDashboardScene());
            case FILE_UPLOAD -> stage.setScene(view.getFileUploadScene());
            case SETTINGS -> stage.setScene(view.getSettingsScene());
            case MANUAL -> stage.setScene(view.getManualScene());
            case USER_MANAGER -> stage.setScene(view.getUserManagerScene());
        }
    }

    /**
     * Handles the changing of metrics on the charts
     * @param viewIndex Chart whose metric is to be changed
     * @param metric Metric to be displayed on the chart
     * @param index Identifies whether the metric is to be overlain or not.
     *              If index == 0, it should not be overlain. If index == 1, it should.
     */
    private void handleFilters(int viewIndex, Metric metric, int index) {
        int graphIndex = viewIndex + index;
        if(viewIndex == 1 || viewIndex == 2) graphIndex += 1;

        view.setTotalText(graphIndex, metric);

        // Update current metrics array with the changed metrics.
        if (viewIndex == 1) {
            currentMetrics[2] = metric;
        } else if (viewIndex == 2) {
            currentMetrics[3] = metric;
        } else {
            currentMetrics[index] = metric;
        }

        setTitles(0);
        setTitles(1);
        setTitles(2);
        loadChartData(viewIndex);
    }

    private void setTitles(int viewIndex){
        String gender = "";
        if(selectedGenders.length > 0) gender = " where users are " + String.join(", ", selectedGenders).toLowerCase();

        String ages = "";
        if(selectedAges.length > 0) ages = " where users are ages " + String.join(", ", selectedAges).toLowerCase();

        String income = "";
        if(selectedIncomes.length > 0) income = " where users have " + String.join(", ", selectedIncomes).toLowerCase() + " income";

        String context = "";
        if(selectedContexts.length > 0) context = " where users have " + String.join(", ", selectedContexts).toLowerCase() + " context";

        String dates = "";
        if (selectedDates[0] == null) {
            dates = " between " + DataModel.getEarliestAndLatestDates()[0].toString() + " and " + DataModel.getEarliestAndLatestDates()[1].toString() + ".";
        } else {
            dates = " between " + selectedDates[0].toString() + " and " + selectedDates[1].toString() + ".";
        }

        ages = ages.replace("@lt;", "<");
        ages = ages.replace("@gt;", ">");

        String filter = gender + ages + income+context + dates;

        String timeTitle = " by " + currentTimeGranularity.toString().toLowerCase();

        String overlayTitle = "";
        if (currentMetrics[1] != null) overlayTitle = " overlain with the " + currentMetrics[1].toString().toLowerCase();

        String metricTitle0 = "";
        if(currentMetrics[0] != null) metricTitle0 = "the " + currentMetrics[0].toString().toLowerCase();

        String metricTitle1 = "";
        if(currentMetrics[2] != null) metricTitle1 = "the " + currentMetrics[2].toString().toLowerCase();

        String metricTitle2 = "";
        if(currentMetrics[3] != null) metricTitle2 = "the " + currentMetrics[3].toString().toLowerCase();

        if (viewIndex == 0){
            chartTitles[0].set("Graph to show " + metricTitle0 + overlayTitle + timeTitle + filter);
        }
        else if (viewIndex == 1){
            chartTitles[1].set("Graph to show " + metricTitle1 + timeTitle + filter);
        }
        else{
            chartTitles[2].set("Graph to show " + metricTitle2 + timeTitle + filter);
        }
    }

    /**
     * Handles the changing of time granularity on the chart
     * @param timeGranularity TimeGranularity to be displayed on the chart
     */
    private void handleTimeGranularity(TimeGranularity timeGranularity) {
        currentTimeGranularity = timeGranularity; // Set new time granularity

        // Reload chart data with the new time granularity for chart one
        loadChartData(0);

        // Reload chart data with the new time granularity for charts two and three if they are being used
        if (view.isLineChart1Visible()) {
            loadChartData(1);
        }
        if (view.isLineChart2Visible()) {
            loadChartData(2);
        }

        setTitles(0);
        setTitles(1);
        setTitles(2);
    }

    private void handleFileUpload(String path, FileType fileType) {
        switch (fileType){
            case CLICK -> {
                clickLogDatabase.dropTable();
                clickLogDatabase.createTable();
                clickLogDatabase.importData(path);
            }
            case IMPRESSION -> {
                impressionLogDatabase.dropTable();
                impressionLogDatabase.createTable();
                impressionLogDatabase.importData(path);
            }
            case SERVER -> {
                serverLogDatabase.dropTable();
                serverLogDatabase.createTable();
                serverLogDatabase.importData(path);
            }
        }
    }

    public void setFilters(int viewIndex, String[] gender, String[] age, String[] income, String[] context, LocalDate startDate, LocalDate endDate) {
        System.out.println("Setting filters for view " + viewIndex);

        selectedGenders = gender;
        selectedAges = age;
        selectedIncomes = income;
        selectedContexts=context;
        selectedDates[0] = startDate;
        selectedDates[1] = endDate;

        setTitles(viewIndex);

        loadChartData(viewIndex);
    }

    /**
     * Loads data into the chart based on the time granularity and metric displayed
     * @param viewIndex Chart whose metric is to be changed
     */
    void loadChartData(int viewIndex) {
        System.out.println("Loading chart data for view " + viewIndex);

	    //this.impressionLogPath = impressionLogPath;
	    //this.clickLog = clickLog;
	    //this.serverLog = serverLog;

	    // Set correct labels for the current time granularity
	    loadTimeGranularity(currentTimeGranularity);

      Metric[] metrics = currentMetrics;
      String[] ages = selectedAges;
      String[] genders = selectedGenders;
      String[] incomes = selectedIncomes;
      String[] context = selectedContexts;
      LocalDate[] dates;
      if (selectedDates[0] != null && selectedDates[1] != null) {
          dates = selectedDates;
      } else {
          dates = DataModel.getEarliestAndLatestDates();
      }

	    new Thread(() -> {
          view.showGraphLoading(viewIndex, true);
		    xAxis = "";
		    yAxis = "";
		    loadChart(viewIndex, 0, metrics, genders, ages, incomes,context, dates);

          view.showGraphLoading(viewIndex, false);


		    xAxis += "/";
		    yAxis += "/";

          view.showGraphLoading(0, true);
		    loadChart(0 ,1, metrics, genders, ages, incomes,context, dates);
          view.showGraphLoading(0, false);
	    }).start();

	    view.hideFields();

	    if(Arrays.asList(currentMetrics).contains(Metric.BOUNCE_RATE) ||
			    Arrays.asList(currentMetrics).contains(Metric.BOUNCES)){
		    view.showBounceFields();
	    }
    }

    private void loadTimeGranularity(TimeGranularity timeGranularity){
        if(timeGranularity == null) return;
        String display = timeToString(timeGranularity);

        view.setXAxis(display, 0);
        view.setXAxis(display, 1);
        view.setXAxis(display, 2);
    }

    private String timeToString(TimeGranularity timeGranularity){
        return switch (timeGranularity) {
	        case ALL -> "All";
	        case HOUR -> "Hour";
            case DAY -> "Day";
            case MONTH -> "Month";
        };
    }

    private void loadChart(int viewIndex, int graphIndex, Metric[] currentMetrics, String[] selectedGenders, String[] selectedAges, String[] selectedIncomes,String[] selectedContexts, LocalDate[] selectedDates){
        if(currentMetrics == null || currentMetrics[graphIndex] == null) return;

        // metricIndex is used to access the correct metric from metricsDisplayed, based on which graph is being used.
        int metricIndex;
        if (viewIndex == 1 || viewIndex == 2) {metricIndex = viewIndex + 1;}
        else {metricIndex = graphIndex;}

        String display = "Select Metric";

        TreeMap<String, ? extends Number> data = null;

        Graph graph = null;

        // If the first graph is displaying a metric, and is not a histogram, another metric can be chosen to overlay it.
        if (currentMetrics[0] != null && currentMetrics[0] != Metric.COST_DISTRIBUTION)
            view.showMetric2Button();
        else {view.hideMetric2Button();}

        // If the graph to be changed is to be changed to a histogram, make the histogram visible and the line chart in
        // its place invisible, and vice versa.
        if(currentMetrics[metricIndex] == null) return;

        if (currentMetrics[metricIndex] == Metric.COST_DISTRIBUTION) {
            view.refreshCharts(Graph.HISTOGRAM, viewIndex);
        } else {
            view.refreshCharts(Graph.LINE, viewIndex);
        }

        // Load data into chart
        switch (currentMetrics[metricIndex]) {
            case IMPRESSIONS -> {
                graph = Graph.LINE;
                data = DataModel.computeNumberOfImpressionsDB(selectedAges, selectedGenders,
                    selectedIncomes,selectedContexts, selectedDates, currentTimeGranularity);

                display = "Number of Impressions";
                xAxis += timeToString(currentTimeGranularity);
                yAxis += display;
            }
            case CLICKS -> {
                graph = Graph.LINE;
                data = DataModel.computeNumberOfClicksDB(selectedAges, selectedGenders,
                    selectedIncomes,selectedContexts, selectedDates, currentTimeGranularity);

                display = "Number of Clicks";
                xAxis += timeToString(currentTimeGranularity);
                yAxis += display;
            }
            case AVG_CLICKS -> {
                graph = Graph.LINE;
                data = DataModel.computeAvgClicksPerImpression(selectedAges, selectedGenders,
                    selectedIncomes,selectedContexts, selectedDates, currentTimeGranularity);

                display = "Average Clicks Per Impression";
                xAxis += timeToString(currentTimeGranularity);
                yAxis += display;
            }
            case COST -> {
                graph = Graph.LINE;
                data = DataModel.computeCostsDB(selectedAges, selectedGenders,
                    selectedIncomes,selectedContexts, selectedDates, currentTimeGranularity);

                display = "Total Cost Per Click";
                xAxis += timeToString(currentTimeGranularity);
                yAxis += display;
            }
            case CONVERSIONS -> {
                graph = Graph.LINE;
                data = DataModel.computeNumberOfConversionsDB(selectedAges, selectedGenders, selectedIncomes,selectedContexts, selectedDates, currentTimeGranularity);

                display = "Number of Conversions";
                xAxis += timeToString(currentTimeGranularity);
                yAxis += display;
            }
            case CPM -> {
                graph = Graph.LINE;
                data = DataModel.computeCPM(selectedAges, selectedGenders, selectedIncomes,selectedContexts, selectedDates, currentTimeGranularity);

                display = "CPM";
                xAxis += timeToString(currentTimeGranularity);
                yAxis += display;
            }
            case UNIQUES -> {
                graph = Graph.LINE;
                data = DataModel.computeUniqueUsersDB(selectedAges, selectedGenders, selectedIncomes,selectedContexts, selectedDates, currentTimeGranularity);

                display = "Total Unique Users";
                xAxis += timeToString(currentTimeGranularity);
                yAxis += display;
            }
            case BOUNCES -> {
                graph = Graph.LINE;
                data = DataModel.computeNumberOfBouncesDB(selectedAges, selectedGenders, selectedIncomes,selectedContexts, selectedDates, currentTimeGranularity, view.getBounceTime(), view.getPagesViewed());

                display = "Total Bounces";
                xAxis += timeToString(currentTimeGranularity);
                yAxis += display;
            }
            case COST_PER_CLICK -> {
                graph = Graph.LINE;
                data = DataModel.computeCostPerClick(selectedAges, selectedGenders, selectedIncomes,selectedContexts, selectedDates, currentTimeGranularity);

                display = "Cost Per Click";
                xAxis += timeToString(currentTimeGranularity);
                yAxis += display;
            }
            case BOUNCE_RATE -> {
                graph = Graph.LINE;
                data = DataModel.computeBounceRate(selectedAges, selectedGenders, selectedIncomes,selectedContexts, selectedDates, currentTimeGranularity, view.getBounceTime(), view.getPagesViewed());

                display = "Bounce Rate";
                xAxis += timeToString(currentTimeGranularity);
                yAxis += display;
            }
            case COST_PER_CONVERSION -> {
                graph = Graph.LINE;
                data = DataModel.computeCostPerConversion(selectedAges, selectedGenders, selectedIncomes,selectedContexts, selectedDates, currentTimeGranularity);

                display = "Cost Per Conversion";
                xAxis += timeToString(currentTimeGranularity);
                yAxis += display;
            }
            case COST_DISTRIBUTION -> {
                graph = Graph.HISTOGRAM;
                data = DataModel.computeCostDistribution(selectedAges, selectedGenders, selectedIncomes,selectedContexts, selectedDates, currentTimeGranularity, 1);

                display = "Cost Distribution";
                xAxis += "Click costs";
                yAxis += "Frequency";
            }
        }

        view.setXAxis(xAxis, viewIndex);
        view.setYAxis(yAxis, viewIndex);
        view.setSeriesName(viewIndex, display, graph, graphIndex);
        view.updateChartAsync(viewIndex,data, graph, graphIndex);
    }


    public static <T extends Number> double totalMap(TreeMap<String, T> map){
        double total = 0;
        for (T value : map.values()) {
            total += Double.parseDouble(value.toString());
        }
        return total;
    }

    public static SimpleIntegerProperty getUniqueUserCountProp() {
        return uniqueUserCountProp;
    }
    public static SimpleIntegerProperty getNumberOfClicksProp() {
        return numberOfClicksProp;
    }
    public static SimpleDoubleProperty getAvgClicksPerImpressionProp() {return avgClicksPerImpressionProp;}
    public static SimpleIntegerProperty getNumberOfImpressionsProp() {return numberOfImpressionsProp;}
    public static SimpleDoubleProperty getTotalCostProp() {
        return totalCostProp;
    }
    public static SimpleIntegerProperty getNumberOfConversionsProp() {return numberOfConversionsProp;}
    public static SimpleDoubleProperty getCpmProp() {
        return cpmProp;
    }
    public static SimpleDoubleProperty getTotalBouncesProp() {
        return totalBouncesProp;
    }
    public static SimpleDoubleProperty getCostPerClickProp() {
        return costPerClickProp;
    }

    public static SimpleDoubleProperty getBounceRateProp() {
        return bounceRateProp;
    }

    public static SimpleDoubleProperty getCostPerConversionProp() {
        return costPerConversionProp;
    }
    public static SimpleDoubleProperty getCostDistributionProp() {
        return costDistributionProp;
    }

    public static String[] getSelectedGenders() {
        return selectedGenders;
    }

    public static String[] getSelectedAges() {
        return selectedAges;
    }

    public static String[] getSelectedIncomes() {
        return selectedIncomes;
    }
    public static String[] getSelectedContexts() {
        return selectedContexts;
    }
}

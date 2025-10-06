package main.dash.data;

import static main.dash.data.DataController.totalMap;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javafx.application.Platform;
import main.dash.database.Database;
import main.dash.enums.TimeGranularity;
import main.dash.common.SortRange;

public class DataModel {
    static DateTimeFormatter inFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static double roundToDP = 3;
    private static final String databaseURL = "jdbc:sqlite:databases/metrics.db"; // Database file

    /**
     * Returns a TreeMap containing the total unique users for the given time granulation
     * @param timeGranularity Character representing the time granulation for the returned data.
     * @return TreeMap containing total unique users for the given time granulation.
     * For Hour, key is in format YYYY-MM-DD HH
     * For Day, key is in format YYYY-MM-DD
     * For Month. key is in format YYYY-MM
     */
    public static TreeMap<String, Number> computeUniqueUsersDB(String[] ages, String[] genders, String[] incomes, String[] contexts, LocalDate[] dates, TimeGranularity timeGranularity)
    {
        String time = grouping(timeGranularity, "cLog.Date");
        String addFilters = addFilters(ages, genders, incomes,contexts, dates);
        if(!addFilters.isEmpty()) addFilters = "WHERE " + addFilters;

        String sql = String.format("""
        SELECT Day, COUNT(DISTINCT ID)
        FROM (SELECT %s AS Day, cLog.ID, Gender, Age, Income,Context
            FROM clickLog cLog JOIN impressionsLog iLog ON cLog.ID = iLog.ID)
            AS temp
        %s
        GROUP BY Day
        """, time, addFilters);

        TreeMap<String, Number> result = queryToMap(sql);
	    assert result != null;
	    DataController.getUniqueUserCountProp().set((int)totalMap(result));

        return result;
    }

    /**
     * Returns a TreeMap containing the clicks for the given time granulation
     * @param timeGranularity Character representing the time granulation for the returned data.
     * @return TreeMap containing clicks for the given time granulation.
     * For Hour, key is in format YYYY-MM-DD HH
     * For Day, key is in format YYYY-MM-DD
     * For Month. key is in format YYYY-MM
     */
    public static TreeMap<String, Number> computeNumberOfClicksDB(String[] ages, String[] genders, String[] incomes,String[] contexts, LocalDate[] dates, TimeGranularity timeGranularity){
        String time = grouping(timeGranularity, "cLog.Date");

        String addFilters = addFilters(ages, genders, incomes,contexts, dates);
        if(!addFilters.isEmpty()) addFilters = "WHERE " + addFilters;

        String sql = String.format("""
        SELECT Day, COUNT(*)
        FROM(SELECT DISTINCT cLog.ID, %s AS Day, cLog.ID, Gender, Age, Income, Context
            FROM clickLog cLog JOIN impressionsLog iLog ON cLog.ID = iLog.ID)
            AS temp
        %s
        GROUP BY Day
        """, time, addFilters);
        TreeMap<String, Number> result = queryToMap(sql);
	    assert result != null;
	    DataController.getNumberOfClicksProp().set((int)totalMap(result));
        return result;
    }

    /**
     * Returns a TreeMap containing the average clicks per impression for the given time granulation
     * @param timeGranularity Character representing the time granulation for the returned data.
     * @return TreeMap containing average clicks per impression for the given time granulation.
     * For Hour, key is in format YYYY-MM-DD HH
     * For Day, key is in format YYYY-MM-DD
     * For Month. key is in format YYYY-MM
     */
    public static TreeMap<String, Number> computeAvgClicksPerImpression(String[] ages, String[] genders, String[] incomes,String[] contexts, LocalDate[] dates, TimeGranularity timeGranularity) {
        TreeMap<String, Number> impressionMap = computeNumberOfImpressionsDB(ages, genders, incomes,contexts, dates, timeGranularity); // Map containing impressions
        TreeMap<String, Number> clickMap = computeNumberOfClicksDB(ages, genders, incomes,contexts, dates, timeGranularity); // Map containing clicks
        TreeMap<String, Number> resultsMap = new TreeMap<>(); // Map containing CTR

        double totalAvg = 0;
        for (String date : impressionMap.keySet()) {
            if (clickMap.get(date) != null && impressionMap.get(date) != null) {
                double avg = clickMap.get(date).doubleValue() / impressionMap.get(date).doubleValue();
                resultsMap.put(date, avg);

                totalAvg += avg;
            }
        }

        double multiplier = Math.pow(10, roundToDP);
        DataController.getAvgClicksPerImpressionProp().set(Math.round(totalAvg / resultsMap.size() * multiplier) / multiplier);

        return resultsMap;
    }

    /**
     * Returns a TreeMap containing the impressions for the given time granulation
     * @param timeGranularity Enum representing the time granularity for the returned data.
     * @return TreeMap containing impressions for the given time granulation.
     * For Hour, key is in format YYYY-MM-DD HH
     * For Day, key is in format YYYY-MM-DD
     * For Month. key is in format YYYY-MM
     */
    public static TreeMap<String, Number> computeNumberOfImpressionsDB(String[] ages, String[] genders, String[] incomes,String[] contexts, LocalDate[] dates, TimeGranularity timeGranularity){
        String time = grouping(timeGranularity, "Date");
        String addFilters = addFilters(ages, genders, incomes,contexts, dates);
        if(!addFilters.isEmpty()) addFilters = "WHERE " + addFilters;

        String sql = String.format("""
        SELECT Day, COUNT(*)
        FROM (SELECT %s AS Day, Age, Gender, Income,Context
            FROM impressionsLog)
            AS temp
        %s
        GROUP BY Day
        """, time, addFilters);

        TreeMap<String, Number> result = queryToMap(sql);
              assert result != null;
              DataController.getNumberOfImpressionsProp().set((int)totalMap(result));
              return result;
    }

    /**
     * Returns a TreeMap containing the total costs for the given time granulation
     * @param timeGranulation Character representing the time granulation for the returned data.
     * @return TreeMap containing total costs for the given time granulation.
     * For Hour, key is in format YYYY-MM-DD HH
     * For Day, key is in format YYYY-MM-DD
     * For Month. key is in format YYYY-MM
     */

    public static TreeMap<String, Number> computeCostsDB(String[] ages, String[] genders, String[] incomes,String[] contexts, LocalDate[] dates, TimeGranularity timeGranulation){
        String time = grouping(timeGranulation, "cLog.Date");
        String addFilters = addFilters(ages, genders, incomes,contexts, dates);
        if(!addFilters.isEmpty()) addFilters = "WHERE " + addFilters;

        String sql = String.format("""
        SELECT Day, SUM(ClickCost)
        FROM(SELECT %s AS Day, cLog.ID, Gender, Age, Income,Context, ClickCost
            FROM clickLog cLog JOIN impressionsLog iLog ON cLog.ID = iLog.ID)
            AS temp
        %s
        GROUP BY Day
        """, time, addFilters);

        TreeMap<String, Number> result = queryToMap(sql);
        assert result != null;
        double multiplier = Math.pow(10, roundToDP);
        DataController.getTotalCostProp().set(Math.round(totalMap(result) * multiplier) / multiplier);
        return result;
    }

    /**
     * Returns a TreeMap containing the total conversions for the given time granulation
     * @param timeGranulation Character representing the time granulation for the returned data.
     * @return TreeMap containing total conversions for the given time granulation.
     * For Hour, key is in format YYYY-MM-DD HH
     * For Day, key is in format YYYY-MM-DD
     * For Month. key is in format YYYY-MM
     */
    public static TreeMap<String, Number> computeNumberOfConversionsDB(String[] ages, String[] genders, String[] incomes,String[] contexts, LocalDate[] dates, TimeGranularity timeGranulation){
        String time = grouping(timeGranulation, "EntryDate");

        String addFilters = addFilters(ages, genders, incomes,contexts, dates);
        if(!addFilters.isEmpty()) addFilters = String.format(" AND (%s)", addFilters);

        String sql = String.format("""
        SELECT Day, COUNT(*)
        FROM (SELECT %s AS Day, sLog.ID, Gender, Age, Income,Context, Conversion
            FROM serverLog sLog JOIN impressionsLog iLog ON sLog.ID = iLog.ID)
            AS temp
        WHERE Conversion = TRUE %s
        GROUP BY Day
        """, time, addFilters);

        TreeMap<String, Number> result = queryToMap(sql);
        assert result != null;
        DataController.getNumberOfConversionsProp().set((int)totalMap(result));
        return result;
    }

    /**
     * Returns a TreeMap containing the CPM for the given time granulation
     * @param timeGranulation Character representing the time granulation for the returned data.
     * @return TreeMap containing CPM for the given time granulation.
     * For Hour, key is in format YYYY-MM-DD HH
     * For Day, key is in format YYYY-MM-DD
     * For Month. key is in format YYYY-MM
     */
    public static TreeMap<String, Number> computeCPM(String[] ages, String[] genders, String[] incomes,String[] contexts, LocalDate[] dates, TimeGranularity timeGranulation) {
        TreeMap<String, Number> impressionMap = computeNumberOfImpressionsDB(ages, genders, incomes,contexts, dates, timeGranulation); // Map containing total impressions
        TreeMap<String, Number> costMap = computeCostsDB(ages, genders, incomes,contexts, dates, timeGranulation); // Map containing total cost
        TreeMap<String, Number> resultsMap = new TreeMap<>(); // Map containing CPM

        double totalAvg = 0;

        for (String date : impressionMap.keySet()) {
            if (costMap.get(date) != null && impressionMap.get(date) != null) {
                double average = (costMap.get(date).doubleValue() / impressionMap.get(date).doubleValue()) * 1000;
                resultsMap.put(date, average);
                totalAvg += average;
            }
        }

        double multiplier = Math.pow(10, roundToDP);
        DataController.getCpmProp().set(Math.round(totalAvg / resultsMap.size() * multiplier) / multiplier);
        return resultsMap;
    }

    /**
     * The function calculates the number of bounces.
     * it calculates by checking if the number of pages vied is 1 or less,
     * or the page has been viewed less than x seconds
     * @return will return the total bounce
     */
    public static TreeMap<String, Number> computeNumberOfBouncesDB(String[] ages, String[] genders, String[] incomes,String[] contexts, LocalDate[] dates, TimeGranularity timeGranulation, int bounceTime, int pagesViewed){
        String time = grouping(timeGranulation, "EntryDate");

        String addFilters = addFilters(ages, genders, incomes,contexts, dates);
        if(!addFilters.isEmpty()) addFilters = String.format(" AND (%s)", addFilters);

        String sql = String.format("""
    SELECT Day, COUNT(*)
    FROM
        (SELECT %s AS Day, EntryDate, PagesViewed, ExitDate,
                sLog.ID, Gender, Age, Income,Context
                FROM serverLog sLog JOIN impressionsLog iLog ON sLog.ID = iLog.ID)
                AS temp

    WHERE ((ExitDate IS NULL AND PagesViewed < %s)
            OR (ExitDate IS NOT NULL AND
                (PagesViewed < %s OR TIME(ExitDate) - TIME(EntryDate) <= %s)))
    %s
    GROUP BY Day
    """, time, pagesViewed, pagesViewed, bounceTime, addFilters);

        TreeMap<String, Number> result = queryToMap(sql);
        assert result != null;
        DataController.getTotalBouncesProp().set((int)totalMap(result));
        return result;
    }

    /**
     *The function will calculate the CPC (cost per click)
     * @return will return the CPC
     */

    public static TreeMap<String, Number> computeCostPerClick(String[] ages, String[] genders, String[] incomes,String[] contexts, LocalDate[] dates, TimeGranularity timeGranularity) {
        TreeMap<String, Number> costMap = computeCostsDB(ages, genders, incomes,contexts, dates, timeGranularity);
        TreeMap<String, Number> clicksMap = computeNumberOfClicksDB(ages, genders, incomes,contexts, dates, timeGranularity);
        TreeMap<String, Number> resultsMap = new TreeMap<>();

        double totalAvg = 0;

        for (String date : clicksMap.keySet()) {
            if (costMap.get(date) != null && clicksMap.get(date) != null) {
                double avg = costMap.get(date).doubleValue() / clicksMap.get(date).doubleValue();
                resultsMap.put(date, avg);

                totalAvg += avg;
            }
        }

        double multiplier = Math.pow(10, roundToDP);
        DataController.getCostPerClickProp().set(Math.round(totalAvg / resultsMap.size() * multiplier) / multiplier);
        return resultsMap;
    }

    public static TreeMap<String, Number> computeBounceRate(String[] ages, String[] genders, String[] incomes,String[] contexts, LocalDate[] dates, TimeGranularity timeGranulation, int bounceTime, int pagesViewed) {
        TreeMap<String, Number> bouncesMap = computeNumberOfBouncesDB(ages, genders, incomes,contexts, dates, timeGranulation, bounceTime, pagesViewed);
        TreeMap<String, Number> clicksMap = computeNumberOfClicksDB(ages, genders, incomes,contexts, dates, timeGranulation);
        TreeMap<String, Number> resultsMap = new TreeMap<>();

        double totalAvg = 0;

        for (String date : clicksMap.keySet()) {
            if (bouncesMap.get(date) != null && clicksMap.get(date) != null) {
                double avg = bouncesMap.get(date).doubleValue() / clicksMap.get(date).doubleValue();
                resultsMap.put(date, avg);

                totalAvg += avg;
            }
        }

        double multiplier = Math.pow(10, roundToDP);
        DataController.getBounceRateProp().set(Math.round(totalAvg / resultsMap.size() * multiplier) / multiplier);
        return resultsMap;
    }

    public static TreeMap<String, Number> computeCostPerConversion(String[] ages, String[] genders, String[] incomes,String[] contexts, LocalDate[] dates, TimeGranularity timeGranulation) {
        TreeMap<String, Number> costMap = computeCostsDB(ages, genders, incomes,contexts, dates, timeGranulation);
        TreeMap<String, Number> conversionsMap = computeNumberOfConversionsDB(ages, genders, incomes,contexts, dates, timeGranulation);
        TreeMap<String, Number> result = new TreeMap<>();

        double totalAvg = 0;

        for (String date : conversionsMap.keySet()) {
            if (costMap.get(date) != null && conversionsMap.get(date) != null) {
                double avg = costMap.get(date).doubleValue() / conversionsMap.get(date).doubleValue();
                result.put(date, avg);

                totalAvg += avg;
            }
        }

        double multiplier = Math.pow(10, roundToDP);
        DataController.getCostPerConversionProp().set(Math.round(totalAvg / result.size() * multiplier) / multiplier);
        return result;
    }

    public static TreeMap<String, Number> computeCostDistribution(String[] ages, String[] genders, String[] incomes,String[] contexts, LocalDate[] dates, TimeGranularity timeGranularity, float range) {
        String time = grouping(timeGranularity, "cLog.Date");

        String addFilters = addFilters(ages, genders, incomes,contexts,dates);
        addFilters = addFilters.isEmpty() ? "" : String.format("WHERE %s", addFilters);

        TreeMap<String, Number> costMap = new TreeMap<>(new SortRange());

        String sql = null;
        try (Connection conn = Database.connect(databaseURL)) {
            assert conn != null;

            int max = maxCost(conn, addFilters);

            StringBuilder sqlBins = new StringBuilder();

            for(float i = 0; i < max; i+=range) {
                float lower = i;
                float upper = i + range;

                String key = String.format("%s-%s", lower, upper);

                sqlBins.append(String.format("WHEN ClickCost BETWEEN %s AND %s THEN '%s'\n", lower, upper, key));
                costMap.put(key, 0);
            }

            sql = String.format("""
								SELECT
                CASE
                %s
                END as range,
								COUNT(*)
								FROM (SELECT cLog.ID, %s AS Day, Gender, Age, Income,Context, ClickCost
												FROM clickLog cLog JOIN impressionsLog iLog ON cLog.ID = iLog.ID)
												AS temp
								%s
                GROUP BY range
								""", sqlBins, time, addFilters);

            try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                ResultSet results = Database.queryTable(databaseURL, preparedStatement);

                assert results != null;

                while (results.next()) {
                    String rangeKey = results.getString(1);
                    int count = results.getInt(2);

                    if (costMap.containsKey(rangeKey)) {
                        costMap.put(rangeKey, count);
                        System.out.println(rangeKey + ", " + count);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error querying: " + e.getMessage() + "\n\n" + sql);
        }

        double multiplier = Math.pow(10, roundToDP);
        DataController.getCostDistributionProp().set(Math.round(totalMap(costMap) * multiplier) / multiplier);
        return costMap;
    }

    private static int maxCost(Connection conn, String addFilters){
        String sql = String.format("""
            SELECT MAX(ClickCost)
            FROM (SELECT cLog.ID, cLog.Date AS Day, Gender, Age, Income,Context, ClickCost
                FROM clickLog cLog JOIN impressionsLog iLog ON cLog.ID = iLog.ID)
                AS temp
            %s
            """, addFilters);

        int max = 0;
        try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            ResultSet results = Database.queryTable(databaseURL, preparedStatement);

            assert results != null;

            results.next();
            max = (int)Math.ceil(results.getFloat(1));
        } catch (SQLException e) {
            System.out.println("Error querying: " + e.getMessage() + "\n\n" + sql);
        }

        return max;
    }


    private static String addFilters(String[] ages, String[] genders, String[] incomes,String[] contexts, LocalDate[] dates){

        StringBuilder sql = new StringBuilder();
        String ageFilter = addFilter("Age", ages);
        String genderFilter = addFilter("Gender", genders);
        String incomeFilter = addFilter("Income", incomes);
        String contextFilter = addFilter("Context", contexts);
        String dateFilter = addFilter("Day",
            new String[]{String.valueOf(dates[0]), String.valueOf(dates[1])});

        if(!ageFilter.isEmpty()){
            sql.append("(").append(ageFilter).append(")");
        }

        if(!genderFilter.isEmpty()) {
            if(!sql.isEmpty()) sql.append(" AND ");
            sql.append("(").append(genderFilter).append(")");
        }

        if(!incomeFilter.isEmpty()) {
            if(!sql.isEmpty()) sql.append(" AND ");
            sql.append("(").append(incomeFilter).append(")");
        }
        if(!contextFilter.isEmpty()) {
            if(!sql.isEmpty()) sql.append(" AND ");
            sql.append("(").append(contextFilter).append(")");
        }

        if(!dateFilter.isEmpty()) {
            if(!sql.isEmpty()) sql.append(" AND ");
            sql.append("(").append(dateFilter).append(")");
        }

        return sql.toString();
    }

    private static String addFilter(String column, String[] filter){
        StringBuilder formatted = new StringBuilder();

        if(Objects.equals(column, "Day")){
            formatted.append(column);
            formatted.append(" BETWEEN \"");
            formatted.append(filter[0]);
            formatted.append("\" AND \"");
            formatted.append(filter[1]);
            formatted.append("\"");
        } else {
            for (int i = 0; i < filter.length; i++) {
                formatted.append(column);
                formatted.append(" = \"");

                filter[i] = filter[i].replace("@lt;", "<");
                filter[i] = filter[i].replace("@gt;", ">");
                formatted.append(filter[i]).append("\"");

                if (i < filter.length - 1) {
                    formatted.append(" OR ");
                }
            }
        }
        return formatted.toString();
    }


    private static TreeMap<String, Number> queryToMap(String sql){
        try (Connection conn = Database.connect(databaseURL)) {
            assert conn != null;
            try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                ResultSet results = Database.queryTable(databaseURL, preparedStatement);

                assert results != null;

                TreeMap<String, Number> resultsMap = new TreeMap<>();
                while(results.next()){
                    String date = results.getString(1);
                    Double id = results.getDouble(2);
                    resultsMap.put(date, id);
                }

                return resultsMap;
            }
        } catch (SQLException e) {
            System.out.println("Error querying: " + e.getMessage() + "\n\n" + sql);
        }

        return new TreeMap<>();
    }

    public static double getDP() {
        return roundToDP;
    }

    private static String grouping(TimeGranularity timeGranularity, String column){
        return switch (timeGranularity) {
            case ALL -> "strftime('%Y-%m-%d %H:%M:%S', " + column + ")";
            case HOUR -> "strftime('%Y-%m-%d %H:00:00', " + column + ")";
            case DAY -> "strftime('%Y-%m-%d', " + column + ")";
            case MONTH -> "strftime('%Y-%m-01', " + column + ")";
        };
    }

    public static LocalDate[] getEarliestAndLatestDates() {
        String sql = """
            SELECT MIN(Date) AS earliestDate, MAX(Date) AS latestDate
            FROM impressionsLog
            """;
        LocalDate[] dates = new LocalDate[2];

        try (Connection conn = Database.connect(databaseURL);
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            ResultSet results = Database.queryTable(databaseURL, preparedStatement);

            assert results != null;
            if (results.next()) {
                dates[0] = results.getDate("earliestDate").toLocalDate();
                dates[1] = results.getDate("latestDate").toLocalDate();
            }
        } catch (SQLException e) {
            System.err.println("Error executing SQL query: " + e.getMessage());
        }
        return dates;
    }
}
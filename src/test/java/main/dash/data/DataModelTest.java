package main.dash.data;

import io.vavr.collection.Tree;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.TreeMap;
import javax.xml.crypto.Data;
import main.dash.enums.TimeGranularity;
import org.assertj.core.internal.bytebuddy.asm.Advice.Local;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DataModelTest {

    @Test
    void testNoFilters(){
        testFuncs(
            "valid",
            new String[]{},
            new String[]{},
            new String[]{},
            new String[]{},
            new LocalDate[]{LocalDate.EPOCH, LocalDate.now()},
            TimeGranularity.ALL,
            0,
            0,
            1
        );
    }

    @Test
    void testMonth(){
        testFuncs(
            "valid",
            new String[]{},
            new String[]{},
            new String[]{},
            new String[]{},
            new LocalDate[]{LocalDate.EPOCH, LocalDate.now()},
            TimeGranularity.MONTH,
            0,
            0,
            1
        );
    }

    @Test
    void testRange(){
        testFuncs(
            "valid",
            new String[]{},
            new String[]{},
            new String[]{},
            new String[]{},
            new LocalDate[]{LocalDate.of(2015, 1, 2)
                , LocalDate.of(2015, 1, 11)},
            TimeGranularity.ALL,
            0,
            0,
            1
        );
    }

    @Test
    void testAges(){
        testFuncs(
            "valid",
            new String[]{"25-34", "45-54"},
            new String[]{},
            new String[]{},
            new String[]{},
            new LocalDate[]{LocalDate.EPOCH, LocalDate.now()},
            TimeGranularity.ALL,
            0,
            0,
            1
        );
    }


    @Test
    void testGenders(){
        testFuncs(
            "valid",
            new String[]{},
            new String[]{"Female"},
            new String[]{},
            new String[]{},
            new LocalDate[]{LocalDate.EPOCH, LocalDate.now()},
            TimeGranularity.ALL,
            0,
            0,
            1
        );
    }

    @Test
    void testIncomes(){
        testFuncs(
            "valid",
            new String[]{},
            new String[]{},
            new String[]{"Low", "High"},
            new String[]{},
            new LocalDate[]{LocalDate.EPOCH, LocalDate.now()},
            TimeGranularity.ALL,
            0,
            0,
            1
        );
    }

    @Test
    void testContexts(){
        testFuncs(
            "valid",
            new String[]{},
            new String[]{},
            new String[]{},
            new String[]{"News", "Shopping"},
            new LocalDate[]{LocalDate.EPOCH, LocalDate.now()},
            TimeGranularity.ALL,
            0,
            0,
            1
        );
    }

    @Test
    void testBounceTime(){
        testFuncs(
            "valid",
            new String[]{},
            new String[]{},
            new String[]{},
            new String[]{},
            new LocalDate[]{LocalDate.EPOCH, LocalDate.now()},
            TimeGranularity.ALL,
            2,
            0,
            1
        );
    }

    @Test
    void testPagesViewed(){
        testFuncs(
            "valid",
            new String[]{},
            new String[]{},
            new String[]{},
            new String[]{},
            new LocalDate[]{LocalDate.EPOCH, LocalDate.now()},
            TimeGranularity.ALL,
            0,
            3,
            1
        );
    }


    @Test
    void testDistributionRange(){
        testFuncs(
            "valid",
            new String[]{},
            new String[]{},
            new String[]{},
            new String[]{},
            new LocalDate[]{LocalDate.EPOCH, LocalDate.now()},
            TimeGranularity.ALL,
            0,
            0,
            0.1f
        );
    }


    @Test
    void testEverything(){
        testFuncs(
            "valid",
            new String[]{"&lt;25", "&gt;54", "45-54", "25-34"},
            new String[]{"Male"},
            new String[]{"Medium", "High"},
            new String[]{"Blog", "Social Media", "Shopping"},
            new LocalDate[]{LocalDate.of(2015, 1, 3)
                , LocalDate.of(2015, 1, 13)},
            TimeGranularity.DAY,
            5,
            2,
            0.1f
        );
    }

    @Test
    void testInvalidDates(){
        testFuncs(
            "invalid",
            new String[]{},
            new String[]{},
            new String[]{},
            new String[]{},
            new LocalDate[]{LocalDate.now(), LocalDate.EPOCH},
            TimeGranularity.ALL,
            0,
            0,
            1
        );
    }

    @Test
    void testUpperBoundary(){
        testFuncs(
            "invalid",
            new String[]{},
            new String[]{},
            new String[]{},
            new String[]{},
            new LocalDate[]{LocalDate.of(2015, 1, 15)
                , LocalDate.of(2015, 1, 15)},
            TimeGranularity.ALL,
            0,
            0,
            1
        );
    }

    @Test
    void testLowerBoundary(){
        testFuncs(
            "invalid",
            new String[]{},
            new String[]{},
            new String[]{},
            new String[]{},
            new LocalDate[]{LocalDate.of(2014, 12, 31)
                , LocalDate.of(2014, 12, 31)},
            TimeGranularity.ALL,
            0,
            0,
            1
        );
    }

    void testFuncs(String func, String[] ages, String[] genders, String[] incomes, String[] contexts, LocalDate[] dates, TimeGranularity timeGranularity, int bounceTime, int pagesViewed, float distributionRange){
        System.out.println("------ TEST DATA SUMMARY ------");

        System.out.printf("Filters applied: ages: %s\ngenders:%s\nincomes:%s\ncontexts:%s\ndates:%s\ntime:%s",
            String.join(", ", ages), String.join(",", genders), String.join(",", incomes),
            String.join(",", contexts), Arrays.toString(dates), timeGranularity);

        testFunc("Number of Unique Users: ", DataModel.computeUniqueUsersDB(ages, genders, incomes, contexts, dates, timeGranularity), func);
        testFunc("Number of Clicks: ", DataModel.computeNumberOfClicksDB(ages, genders, incomes, contexts, dates, timeGranularity), func);
        testFunc("Average Clicks per Impression: ", DataModel.computeAvgClicksPerImpression(ages, genders, incomes, contexts, dates, timeGranularity), func);
        testFunc("Number of Impressions: ", DataModel.computeNumberOfImpressionsDB(ages, genders, incomes, contexts, dates, timeGranularity), func);
        testFunc("Cost: ", DataModel.computeCostsDB(ages, genders, incomes, contexts, dates, timeGranularity), func);
        testFunc("Number of Conversions: ", DataModel.computeNumberOfConversionsDB(ages, genders, incomes, contexts, dates, timeGranularity), func);
        testFunc("CPM (Cost per 1000 Impressions): ", DataModel.computeCPM(ages, genders, incomes, contexts, dates, timeGranularity), func);
        testFunc("Number of Bounces: ", DataModel.computeNumberOfBouncesDB(ages, genders, incomes, contexts, dates, timeGranularity, bounceTime, pagesViewed), func);
        testFunc("Rate of Bounces: ", DataModel.computeBounceRate(ages, genders, incomes, contexts, dates, timeGranularity, bounceTime, pagesViewed), func);
        testFunc("CPC (Cost per Click): ", DataModel.computeCostPerClick(ages, genders, incomes, contexts, dates, timeGranularity), func);
        testFunc("Cost distribution: ", DataModel.computeCostDistribution(ages, genders, incomes, contexts, dates, timeGranularity, distributionRange), func);
        System.out.println("--------------------------------");
    }

    void testFunc(String metric, TreeMap<String, Number> data, String func){
        switch(func){
            case "invalid":
                testInvalid(metric, data);
                break;
            case "valid":
                testValid(metric, data);
                break;
            default:
                System.out.println("Invalid function name");
        }
    }

    void testInvalid( String metric, TreeMap<String, Number> data){
        System.out.println(metric + "Testing invalid input");
        assertTrue(data.isEmpty(), metric + " should be empty.");
        System.out.println(metric + " test passed!");
    }

    void testValid(String metric, TreeMap<String, Number> data){
        System.out.println(metric + "Testing valid input");
        assertFalse(data.isEmpty(), metric + " should not be empty.");
        assertTrue(data.values().stream().allMatch(value -> value.doubleValue() >= 0), metric + "All values should be non-negative.");
        System.out.println(metric + "passed!");
    }
}



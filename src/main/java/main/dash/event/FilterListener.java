package main.dash.event;


import java.time.LocalDate;

public interface FilterListener {
    /**
     * Data filters
     * @param viewIndex the index of data view
     * @param genders the gender arr
     * @param ages the age arr
     * @param incomes the income arr
     */
    void filterApplied(int viewIndex, String[] genders, String[] ages, String[] incomes,String[] contexts, LocalDate startDate, LocalDate endDate);
}

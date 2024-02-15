package org.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class employee {
//    public static String determineEmployeeStatus(String start, String livePeriod) {
//        // Adjust the formatter to match the full date format
//        DateTimeFormatter fullDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//        LocalDate startDateLocalDate = LocalDate.parse(start, fullDateFormatter);
//        YearMonth startDate = YearMonth.from(startDateLocalDate);
//
//        // Ensure livePeriod is in "yyyy-MM" format as expected
//        YearMonth liveDate = YearMonth.parse(livePeriod, DateTimeFormatter.ofPattern("yyyy-MM"));
//
//        // Logic to determine status
//        if (!startDate.isBefore(liveDate)) {
//            return "new";
//        }else if (startDate.plusMonths(1).equals(liveDate)) {
//            return "active";
//        }
//        // Implement logic for "leaving" and "terminated" statuses as needed
//
//        return "active"; // Default return, adjust as necessary
//    }

    public static String determineEmployeeStatus(String start, String livePeriod, String terminationDate) {
        DateTimeFormatter fullDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDateLocalDate = LocalDate.parse(start, fullDateFormatter);
        YearMonth startDate = YearMonth.from(startDateLocalDate);
        YearMonth liveDate = YearMonth.parse(livePeriod, DateTimeFormatter.ofPattern("yyyy-MM"));

        // Check if terminationDate is provided and not null
        if (terminationDate != null && !terminationDate.isEmpty()) {
            LocalDate terminationDateLocalDate = LocalDate.parse(terminationDate, fullDateFormatter);
            YearMonth terminationDateYearMonth = YearMonth.from(terminationDateLocalDate);

            // Check if the termination date is equal to the live period
            if (terminationDateYearMonth.equals(liveDate)) {
                return "leaving";
            }
            // Check if the live period is after the termination date by exactly one month
            else if (liveDate.equals(terminationDateYearMonth.plusMonths(1))) {
                return "terminated";
            }
        }

        // Existing logic for determining "new" and "active" status
        if (!startDate.isBefore(liveDate)) {
            return "new";
        } else if (startDate.plusMonths(1).equals(liveDate)) {
            return "active";
        }

        // Default to "active" if none of the conditions match
        return "active"; // Adjust this default return as necessary based on your application's requirements
    }


    public static String fetchLivePeriod(Connection connection) {
        try {
            // Specify the columns you want to fetch from the period table
            List<String> columns = Arrays.asList("period");

            // Assuming the status 'Live' is stored in a column named 'status'
            // and you're only interested in fetching the period marked as 'Live'
            String whereClause = "status = 'Live'";

            // Call selectperiod from the period controller
            JSONArray results = period.selectperiod(connection, "period", columns, whereClause, null, null, null, null, null, null, "MySQL");

            if (results.length() > 0) {
                // Assuming only one period is marked as 'Live' at any given time
                JSONObject livePeriod = results.getJSONObject(0);
                return livePeriod.getString("period");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // or throw an exception if appropriate
    }




    public static void Insertemployee(String tableName, Map<String, String> fieldValues, Connection connection) {
        try {
            String insertSQL = QueryManager.constructInsertStatement(tableName, fieldValues);
            PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
            int paramIndex = 1;
            for (String fieldName : fieldValues.keySet()) {
                preparedStatement.setObject(paramIndex++, fieldValues.get(fieldName));
            }

            preparedStatement.executeUpdate();
            System.out.println("Data saved successfully in table " + tableName);
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateemployee(String tableName, String idColumn, int idValue, Map<String, String> fieldValues, Connection connection) {
        try {
            boolean useCurrentTimestamp = "CURRENT_TIMESTAMP".equals(fieldValues.get("date_modified"));

            // Construct the update statement
            String updateSQL = QueryManager.constructUpdateStatement(tableName, idColumn, idValue, fieldValues, useCurrentTimestamp);

            PreparedStatement preparedStatement = connection.prepareStatement(updateSQL);

            int paramIndex = 1;
            for (String key : fieldValues.keySet()) {

                if (!(useCurrentTimestamp && "date_modified".equals(key))) {
                    preparedStatement.setObject(paramIndex++, fieldValues.get(key));
                }
            }

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Record updated successfully in table " + tableName);
            } else {
                System.out.println("No record updated.");
            }
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static JSONArray selectemployee(Connection connection, String tableName, List<String> columns,
                                          String whereClause, String groupBy, String orderBy, String havingClause, Integer limit, Integer offset,
                                          List<String> joinClauses, String databaseType) throws SQLException {
        return QueryManager.dynamicSelect(connection, tableName, columns, whereClause, groupBy, orderBy, havingClause, limit, joinClauses, databaseType, offset);
    }



}

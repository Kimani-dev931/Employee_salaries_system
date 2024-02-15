package org.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class earnings {


    public static int fetch_salary_earnings(Connection connection) {
        try {
            // Specify the columns you want to fetch from the earnings table
            List<String> columns = Arrays.asList("earnings_amount");

            // Condition to select the specific earnings record
            String whereClause = "earnings_id = '19'";

            // Call selectperiod from the period controller
            JSONArray results = period.selectperiod(connection, "earnings", columns, whereClause, null, null, null, null, null, null, "MySQL");

            if (results.length() > 0) {
                JSONObject salary = results.getJSONObject(0);
                return salary.getInt("earnings_amount"); // Use getInt for integer values
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0; // Return a default value or consider throwing an exception
    }
    public static Integer fetchLivePeriodId(Connection connection) {
        try {
            // Specify the column you want to fetch from the period table
            List<String> columns = Arrays.asList("period_id");

            // Assuming the status 'Live' is stored in a column named 'status'
            // and you're only interested in fetching the period_id marked as 'Live'
            String whereClause = "status = 'Live'";

            // Use the dynamicSelect method from the QueryManager to perform the selection
            JSONArray results = period.selectperiod(connection, "period", columns, whereClause, null, null, null, null, null, null, "MySQL");

            if (results.length() > 0) {
                // Assuming only one period is marked as 'Live' at any given time
                JSONObject livePeriod = results.getJSONObject(0);
                return livePeriod.getInt("period_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // or throw an exception if appropriate
    }

    public static void Insert_earnings(String tableName, Map<String, String> fieldValues, Connection connection) {
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

    public static void update_earnings(String tableName, String idColumn, int idValue, Map<String, String> fieldValues, Connection connection) {
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

    public static JSONArray select_earnings(Connection connection, String tableName, List<String> columns,
                                               String whereClause, String groupBy, String orderBy, String havingClause, Integer limit, Integer offset,
                                               List<String> joinClauses, String databaseType) throws SQLException {
        return QueryManager.dynamicSelect(connection, tableName, columns, whereClause, groupBy, orderBy, havingClause, limit, joinClauses, databaseType, offset);
    }





}

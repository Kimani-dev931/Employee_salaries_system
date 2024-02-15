package org.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class rollover {

    public static void rollover(Connection connection) {
        String currentLivePeriod = employee.fetchLivePeriod(connection);
        if (currentLivePeriod != null) {
            // Update the current live period's status to 'Closed'
            updatePeriodStatusToClosed(connection);
            // Calculate the next period
            String nextPeriod = calculateNextPeriod(currentLivePeriod);
            // Insert the new period with status 'Live'
            insertNewLivePeriod(connection, nextPeriod);

            try {
                updateAllEmployeeStatuses(connection, currentLivePeriod);
            } catch (SQLException e) {

                e.printStackTrace();
            }

            processEmployeePayments(connection);


        }
    }

    private static void updatePeriodStatusToClosed(Connection connection) {
        String tableName = "period";
        String idColumn = "status";
        String idValue = "Live";
        Map<String, String> fieldValues = new HashMap<>();
        fieldValues.put("status", "Closed");
        fieldValues.put("date_modified", "CURRENT_TIMESTAMP");
        org.example.period.updateperiod(tableName, idColumn, idValue, fieldValues, connection);

    }

    private static String calculateNextPeriod(String currentPeriod) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        YearMonth current = YearMonth.parse(currentPeriod, formatter);
        YearMonth next = current.plusMonths(1);
        return next.format(formatter);
    }

    private static void insertNewLivePeriod(Connection connection, String period) {
        String tableName = "period";
        Map<String, String> fieldValues = new HashMap<>();
        fieldValues.put("period", period);
        fieldValues.put("status", "Live");

        org.example.period.Insertperiod(tableName, fieldValues, connection);
    }

    //    public static void updateAllEmployeeStatuses(Connection connection, String livePeriod) throws SQLException {
//        // Fetch all employees
//        List<String> columns = Arrays.asList("employee_id", "employee_start_date", "employee_termination_date");
//        JSONArray employees = employee.selectemployee(connection, "employee", columns, "", "", "", "", null, null, Arrays.asList(), "MySQL");
//
//        for (int i = 0; i < employees.length(); i++) {
//            JSONObject emp = employees.getJSONObject(i);
//            int employeeId = emp.getInt("employee_id");
//            Object startDateObj = emp.get("employee_start_date");
//            String start;
//            if (startDateObj != null) {
//                // Assuming the date is correctly formatted as a String
//                start = startDateObj.toString();
//            } else {
//                // Fallback or error handling if the date is null or not in the expected format
//                start = null; // or throw new SQLException("Start date format issue for employee ID: " + employeeId);
//            }
//
//            String termination = emp.optString("employee_termination_date", null); // Use null if termination_date is not present
//            // Determine new status
//            String newStatus = employee.determineEmployeeStatus(start, livePeriod, termination);
//            // Prepare fieldValues for update
//            HashMap<String, String> fieldValues = new HashMap<>();
//            fieldValues.put("employee_status", newStatus);
//            fieldValues.put("date_modified", "CURRENT_TIMESTAMP");
//            // Update the employee record
//            employee.updateemployee("employee", "employee_id", employeeId, fieldValues, connection);
//        }
//    }
    public static void updateAllEmployeeStatuses(Connection connection, String livePeriod) throws SQLException {
        // Fetch all employees, including their current status
        List<String> columns = Arrays.asList("employee_id", "employee_start_date", "employee_termination_date", "employee_status");
        JSONArray employees = employee.selectemployee(connection, "employee", columns, null, null, null, null, null, null, Arrays.asList(), "MySQL");

        for (int i = 0; i < employees.length(); i++) {
            JSONObject emp = employees.getJSONObject(i);
            int employeeId = emp.getInt("employee_id");
            String currentStatus = emp.getString("employee_status"); // Fetch the current status

            // Skip updating the status if it's already "terminated"
            if ("terminated".equals(currentStatus)) {
                continue; // Skip to the next iteration of the loop
            }


            Object startDateObj = emp.get("employee_start_date");
            String start = startDateObj != null ? startDateObj.toString() : null;
            String termination = emp.optString("employee_termination_date", null);

            String newStatus = employee.determineEmployeeStatus(start, livePeriod, termination);

            // Only update if the new status is different from the current status
            if (!newStatus.equals(currentStatus)) {
                HashMap<String, String> fieldValues = new HashMap<>();
                fieldValues.put("employee_status", newStatus);
                fieldValues.put("date_modified", "CURRENT_TIMESTAMP");
                employee.updateemployee("employee", "employee_id", employeeId, fieldValues, connection);
            }
        }
    }

    public static void processEmployeePayments(Connection connection) {
        try {
            connection.setAutoCommit(false); // Start transaction


            String livePeriod = employee.fetchLivePeriod(connection);

            // Fetch all employees
            JSONArray employees = employee.selectemployee(connection, "employee", null, null, null, null, null, null, null, null, "MySQL");

            for (int i = 0; i < employees.length(); i++) {
                JSONObject employee = employees.getJSONObject(i);
                processPaymentForEmployee(connection, employee, livePeriod);
            }

            connection.commit(); // Commit transaction
        } catch (Exception e) {
            try {
                connection.rollback(); // Rollback transaction on error
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                connection.setAutoCommit(true); // Reset auto-commit mode
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void processPaymentForEmployee(Connection connection, JSONObject employee, String livePeriod) throws SQLException {
        // Extract employee details
        int periodId = earnings.fetchLivePeriodId(connection);
        int employeeId = employee.getInt("employee_id");
        String status = employee.getString("employee_status");
        double salary = fetchCurrentSalary(connection, employeeId);

        // Increase salary by 2%
        double newSalary = salary * 1.02;

        // Calculate allowances and deductions
        double houseAllowance = newSalary * 0.03;
        double transportAllowance = newSalary * 0.015;
        double mortgageAllowance = newSalary * 0.02;

        double nhifDeduction = newSalary * 0.0275;
        double nssfDeduction = newSalary * 0.06;
        double payeDeduction = newSalary * 0.14;

        // Check employee status and insert earnings and deductions accordingly
        if (status.equals("new")) {
            // Insert only salary as earnings
            Map<String, String> earningsFields = new HashMap<>();
            earningsFields.put("employee_id", String.valueOf(employeeId));
            earningsFields.put("earning_type_id", "4");
            earningsFields.put("earnings_amount", String.valueOf(newSalary));
            earnings.Insert_earnings("earnings", earningsFields, connection);
        } else if ((status.equals("active") || status.equals("leaving"))) {

            Map<String, String> earningsFields = new HashMap<>();
            earningsFields.put("employee_id", String.valueOf(employeeId));
            earningsFields.put("earning_type_id", "4");
            earningsFields.put("period_id", String.valueOf(periodId));
            earningsFields.put("earnings_amount", String.valueOf(newSalary));
            earnings.Insert_earnings("earnings", earningsFields, connection);

            if (allowanceChecker(employee.has("employee_start_date") ? employee.optString("employee_start_date") : null, livePeriod)) {
                earningsFields.put("employee_id", String.valueOf(employeeId));
                earningsFields.put("earning_type_id", "1");
                earningsFields.put("period_id", String.valueOf(periodId));
                earningsFields.put("earnings_amount", String.valueOf(houseAllowance));
                earnings.Insert_earnings("earnings", earningsFields, connection);

                earningsFields.put("employee_id", String.valueOf(employeeId));
                earningsFields.put("earning_type_id", "2");
                earningsFields.put("period_id", String.valueOf(periodId));
                earningsFields.put("earnings_amount", String.valueOf(transportAllowance));
                earnings.Insert_earnings("earnings", earningsFields, connection);

                earningsFields.put("employee_id", String.valueOf(employeeId));
                earningsFields.put("earning_type_id", "3");
                earningsFields.put("period_id", String.valueOf(periodId));
                earningsFields.put("earnings_amount", String.valueOf(mortgageAllowance));
                earnings.Insert_earnings("earnings", earningsFields, connection);

            }

        }
        // Insert deductions for all except terminated employees
        if (!status.equals("terminated")) {

            Map<String, String> deductionFields = new HashMap<>();
            deductionFields.put("employee_id", String.valueOf(employeeId));
            deductionFields.put("deduction_type_id", "1");
            deductionFields.put("period_id", String.valueOf(periodId));
            deductionFields.put("deduction_amount", String.valueOf(nhifDeduction));
            deductions.Insertdeductions("deductions", deductionFields, connection);

            deductionFields.put("employee_id", String.valueOf(employeeId));
            deductionFields.put("deduction_type_id", "2");
            deductionFields.put("period_id", String.valueOf(periodId));
            deductionFields.put("deduction_amount", String.valueOf(nssfDeduction));
            deductions.Insertdeductions("deductions", deductionFields, connection);

            deductionFields.put("employee_id", String.valueOf(employeeId));
            deductionFields.put("deduction_type_id", "3");
            deductionFields.put("period_id", String.valueOf(periodId));
            deductionFields.put("deduction_amount", String.valueOf(payeDeduction));
            deductions.Insertdeductions("deductions", deductionFields, connection);


        }
    }

    private static boolean allowanceChecker(String startDate, String currentPeriod) {

        LocalDate start = LocalDate.parse(startDate);
        YearMonth startMonth = YearMonth.from(start);
        YearMonth current = YearMonth.parse(currentPeriod);

        return startMonth.plusMonths(3).isBefore(current) || startMonth.plusMonths(3).equals(current);
    }

    private static double fetchCurrentSalary(Connection connection, int employeeId) throws SQLException {
        List<String> columns = Arrays.asList("earnings_amount");
        String whereClause = "employee_id = " + employeeId + " AND earning_type_id = 4";
        String orderBy = "date_created DESC";
        JSONArray results = QueryManager.dynamicSelect(connection, "earnings", columns, whereClause, null, orderBy, null, null, null, "MySQL", null);

        if (results.length() > 0) {
            JSONObject latestEarning = results.getJSONObject(0);
            return latestEarning.getDouble("earnings_amount");
        } else {
            throw new SQLException("Salary for employee ID " + employeeId + " not been initialized.");
        }
    }


}

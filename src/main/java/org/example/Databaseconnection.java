package org.example;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathExpression;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.Key;
import java.util.*;
import java.sql.*;


public class Databaseconnection {

    private static Connection connection = null;
    private static final String SECRET_KEY = "beadc627d00ec777340bf6f06ece360fe1762e8b4408504516afd194dc303c77";

    public static void main(String[] args) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File("config.xml"));

            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();

            List<DatabaseConfig> configs = new ArrayList<>();

            DatabaseConfig config = new DatabaseConfig();

            String databaseType = (String) xpath.compile("/database-config/database-type").evaluate(doc, XPathConstants.STRING);
            config.setDatabaseType(databaseType);

            String databaseName = (String) xpath.compile("/database-config/database-name").evaluate(doc, XPathConstants.STRING);
            config.setDatabaseName(databaseName);

            String databaseHost = (String) xpath.compile("/database-config/database-host").evaluate(doc, XPathConstants.STRING);
            config.setDatabaseHost(databaseHost);

            String username = (String) xpath.compile("/database-config/username/text()").evaluate(doc, XPathConstants.STRING);
            config.setUsername(username);

            String password = (String) xpath.compile("/database-config/password/text()").evaluate(doc, XPathConstants.STRING);
            config.setPassword(password);

            String usernameEncrypted = (String) xpath.compile("/database-config/username/@ENCRYPTED").evaluate(doc, XPathConstants.STRING);
            config.setUsernameEncrypted("YES".equals(usernameEncrypted));

            String passwordEncrypted = (String) xpath.compile("/database-config/password/@ENCRYPTED").evaluate(doc, XPathConstants.STRING);
            config.setPasswordEncrypted("YES".equals(passwordEncrypted));


            // Add the DatabaseConfig object to the list
            configs.add(config);


            String usernameEncryptedAttribute = (String) xpath.compile("/database-config/username/@ENCRYPTED").evaluate(doc, XPathConstants.STRING);
            boolean shouldEncryptUsername = !"YES".equals(usernameEncryptedAttribute);

            // Check if the "ENCRYPTED" attribute is set to "NO" for password
            String passwordEncryptedAttribute = (String) xpath.compile("/database-config/password/@ENCRYPTED").evaluate(doc, XPathConstants.STRING);
            boolean shouldEncryptPassword = !"YES".equals(passwordEncryptedAttribute);
            // Encrypt the username and password if needed
            if (shouldEncryptUsername) {
                config.setUsername(encrypt(config.getUsername(), SECRET_KEY));
                config.setUsernameEncrypted(true);
            }

            if (shouldEncryptPassword) {
                config.setPassword(encrypt(config.getPassword(), SECRET_KEY));
                config.setPasswordEncrypted(true);
            }
            // Update the XML with the modified data
            config.updateXmlElement(doc);


            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("config.xml"));
            transformer.transform(source, result);




            String connectionString = "";
            if ("MySQL".equalsIgnoreCase(config.getDatabaseType())) {
                connectionString = "jdbc:mysql://" + config.getDatabaseHost() + "/" + config.getDatabaseName();

            } else if ("PostgreSQL".equalsIgnoreCase(config.getDatabaseType())) {
                connectionString = "jdbc:postgresql://" + config.getDatabaseHost() + "/" + config.getDatabaseName();

            } else if ("MicrosoftSQL".equalsIgnoreCase(config.getDatabaseType())) {
                connectionString = "jdbc:sqlserver://" + config.getDatabaseHost() + ";databaseName=" + config.getDatabaseName();

            }

            String decryptedUsername = config.isUsernameEncrypted() ? decrypt(config.getUsername(), SECRET_KEY) : config.getUsername();
            String decryptedPassword = config.isPasswordEncrypted() ? decrypt(config.getPassword(), SECRET_KEY) : config.getPassword();

            connection = DriverManager.getConnection(connectionString, decryptedUsername, decryptedPassword);



            rollover.rollover(connection);
//            rollover.processEmployeePayments(connection);


//            String currentLivePeriod = employee.fetchLivePeriod(connection);
//            try {
//                rollover.updateAllEmployeeStatuses(connection, currentLivePeriod);
//            } catch (SQLException e) {
//                // Handle the exception here (e.g., log it, alert the user, etc.)
//                e.printStackTrace();
//            }

//            String tableName = "company";
//            Map<String, String> fieldValues = new HashMap<>();
//            fieldValues.put(" company_name", "Tech Innovations");
//            fieldValues.put("company_reg_number", "REG12345678");
//            fieldValues.put("company_vision", "Innovating for a better tomorrow");
//
//            company.Insertcompany(tableName, fieldValues, connection);

            // This will execute 'SELECT * FROM Student
//            try {
//                List<String> columns = Arrays.asList("company_reg_number");
//
//                JSONArray results = company.selectcompany(connection, "company", columns, null, null, null, null,null,null,null, null);
//                System.out.println(results.toString());
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }




//            String livePeriod = employee.fetchLivePeriod(connection);
//            System.out.println(livePeriod);
//            String employeeStartDate = "2023-03-07";
//            String terminationDate= "2023-07-09";
//            String employeeStatus = employee.determineEmployeeStatus(employeeStartDate,livePeriod,terminationDate);
//
//            String tableName = "employee";
//            Map<String, String> fieldValues = new HashMap<>();
//            fieldValues.put("department_id", "4");
//            fieldValues.put("company_id", "1");
//            fieldValues.put("first_name", "Esther");
//            fieldValues.put("last_name", "Wanjiru");
//            fieldValues.put("phone_number", "0733440420");
//            fieldValues.put("email", "Esther80@gmail.com");
//            fieldValues.put("kra_pin", "A150183483");
//            fieldValues.put("gender", "Female");
//            fieldValues.put("date_of_birth", "1994-08-11");
//            fieldValues.put("employee_termination_date", terminationDate);
//            fieldValues.put("employee_start_date", employeeStartDate);
//            fieldValues.put("employee_status", employeeStatus);
//            fieldValues.put("employee_number", "EMP004");
//            fieldValues.put("user_name", "Esthernjiru");
//            fieldValues.put("password", "Esther83#$%");
//
//
//            employee.Insertemployee(tableName, fieldValues, connection);


//            String tableName = "deduction_type";
//            Map<String, String> fieldValues = new HashMap<>();
//            fieldValues.put("company_id", "1");
//            fieldValues.put("description", "PAYE");
//            deduction_type.Insertdeduction_type(tableName, fieldValues, connection);



//            String tableName = "earning_type";
//            Map<String, String> fieldValues = new HashMap<>();
//            fieldValues.put("company_id", "1");
//            fieldValues.put("description", "salary");
//            earning_type.Insertearning_type(tableName, fieldValues, connection);


//            String tableName = "department";
//            Map<String, String> fieldValues = new HashMap<>();
//            fieldValues.put("company_id", "1");
//            fieldValues.put("department_name", "Finance");
//
//            department.Insertdepartment(tableName, fieldValues, connection);


//            String tableName = "period";
//            Map<String, String> fieldValues = new HashMap<>();
//            fieldValues.put("period", "2023-08");
//            fieldValues.put("status", "Live");
//
//            period.Insertperiod(tableName, fieldValues, connection);


//            String tableName = "earnings";
//            Map<String, String> fieldValues = new HashMap<>();
//            fieldValues.put("employee_id", "6");
//            fieldValues.put("earning_type_id", "4");
//            fieldValues.put("period_id", "1");
//            fieldValues.put("earnings_amount", "48000");
//            earnings.Insert_earnings(tableName, fieldValues, connection);

//
//            int salary = earnings.fetch_salary_earnings(connection);
//            double houseAllowance = 3.0 / 100 * salary;
//            int houseallowance = (int) houseAllowance;
//            String house_Allowance = Integer.toString(houseallowance);
//            System.out.println(house_Allowance);
//            String tableName = "earnings";
//            Map<String, String> fieldValues = new HashMap<>();
//            fieldValues.put("employee_id", "6");
//            fieldValues.put("earning_type_id", "1");
//            fieldValues.put("period_id", "1");
//            fieldValues.put("earnings_amount",house_Allowance);
//            earnings.Insert_earnings(tableName, fieldValues, connection);




//            int salary = earnings.fetch_salary_earnings(connection);
//            double transportAllowance = 1.5 / 100 * salary;
//            int transportallowance = (int) transportAllowance;
//            String transport_Allowance = Integer.toString(transportallowance);
//            System.out.println(transport_Allowance);
//            String tableName = "earnings";
//            Map<String, String> fieldValues = new HashMap<>();
//            fieldValues.put("employee_id", "6");
//            fieldValues.put("earning_type_id", "2");
//            fieldValues.put("period_id", "1");
//            fieldValues.put("earnings_amount",transport_Allowance);
//            earnings.Insert_earnings(tableName, fieldValues, connection);


//            int salary = earnings.fetch_salary_earnings(connection);
//            double mortgageAllowance = 2.0 / 100 * salary;
//            int mortgageallowance = (int) mortgageAllowance;
//            String mortgage_Allowance = Integer.toString(mortgageallowance);
//            System.out.println(mortgage_Allowance);
//            String tableName = "earnings";
//            Map<String, String> fieldValues = new HashMap<>();
//            fieldValues.put("employee_id", "6");
//            fieldValues.put("earning_type_id", "3");
//            fieldValues.put("period_id", "1");
//            fieldValues.put("earnings_amount",mortgage_Allowance);
//            earnings.Insert_earnings(tableName, fieldValues, connection);


//            int salary = earnings.fetch_salary_earnings(connection);
//            double NHIF = 2.75 / 100 * salary;
//            int Nhif = (int) NHIF;
//            String NHIF_deductions = Integer.toString(Nhif);
//            System.out.println(NHIF_deductions);
//            String tableName = "deductions";
//            Map<String, String> fieldValues = new HashMap<>();
//            fieldValues.put("employee_id", "6");
//            fieldValues.put("deduction_type_id", "1");
//            fieldValues.put("period_id", "1");
//            fieldValues.put("deduction_amount",NHIF_deductions);
//            deductions.Insertdeductions(tableName, fieldValues, connection);

//            int salary = earnings.fetch_salary_earnings(connection);
//            double NSSF = 6.0/ 100 * salary;
//            int Nssf = (int) NSSF;
//            String NSSF_deductions = Integer.toString(Nssf);
//            System.out.println(NSSF_deductions);
//            String tableName = "deductions";
//            Map<String, String> fieldValues = new HashMap<>();
//            fieldValues.put("employee_id", "6");
//            fieldValues.put("deduction_type_id", "2");
//            fieldValues.put("period_id", "1");
//            fieldValues.put("deduction_amount",NSSF_deductions);
//            deductions.Insertdeductions(tableName, fieldValues, connection);


//            int salary = earnings.fetch_salary_earnings(connection);
//            double PAYE = 14.0/ 100 * salary;
//            int Paye = (int) PAYE;
//            String PAYE_deductions = Integer.toString(Paye);
//            System.out.println(PAYE_deductions);
//            String tableName = "deductions";
//            Map<String, String> fieldValues = new HashMap<>();
//            fieldValues.put("employee_id", "6");
//            fieldValues.put("deduction_type_id", "3");
//            fieldValues.put("period_id", "1");
//            fieldValues.put("deduction_amount",PAYE_deductions);
//            deductions.Insertdeductions(tableName, fieldValues, connection);



            connection.close();


        } catch (Exception e) {
            e.printStackTrace();
        }finally {

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String encrypt(String input, String key) throws Exception {
        byte[] keyBytes = hexStringToByteArray(key);

        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("Key must be 32 bytes long");
        }

        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] encryptedBytes = cipher.doFinal(input.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private static String decrypt(String encryptedText, String key) throws Exception {
        byte[] keyBytes = hexStringToByteArray(key);

        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("Key must be 32 bytes long");
        }

        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
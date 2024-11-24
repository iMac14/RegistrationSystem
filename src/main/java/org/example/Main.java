package org.example;
import java.sql.*;
public class Main {
    public static void main(String[] args) {
        try {
            // Establish connection
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/login_schema",  // Database URL
                    "root",                                    // MySQL username
                    "03170214@Mac"                              // MySQL password
            );

            // Create statement and execute query
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM UserDetails");

            // Iterate through result set and display data
            while (resultSet.next()) {
                // Print out the username and password from the result set
                System.out.println("Username: " + resultSet.getString("username"));
                System.out.println("Password: " + resultSet.getString("password"));
            }

            // Close resources
            resultSet.close();
            statement.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

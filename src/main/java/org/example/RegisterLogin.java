package org.example;
import java.sql.*;
import java.util.Scanner;

public class RegisterLogin {

    // MySQL database connection details
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/login_schema";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "03170214@Mac";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome! Please choose an option:");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.print("Enter choice (1 or 2): ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline left-over

        if (choice == 1) {
            registerUser(scanner);
        } else if (choice == 2) {
            loginUser(scanner);
        } else {
            System.out.println("Invalid choice.");
        }

        scanner.close();
    }

    // Registration Method
    public static void registerUser(Scanner scanner) {
        System.out.println("\nRegistration Process:");

        System.out.print("Enter your username: ");
        String username = scanner.nextLine();

        System.out.print("Enter your password: ");
        String password = scanner.nextLine();

        System.out.print("Enter your email: ");
        String email = scanner.nextLine();

        System.out.print("Enter your contact number: ");
        String contactNumber = scanner.nextLine();

        System.out.print("Enter your birthday (YYYY-MM-DD): ");
        String birthday = scanner.nextLine();

        if (registerUserInDatabase(username, password, email, contactNumber, birthday)) {
            System.out.println("Registration successful!");
            loginUser(scanner); // Redirect to login after registration
        } else {
            System.out.println("Username already exists. Please try a different username.");
        }
    }

    // Login Method
    public static void loginUser(Scanner scanner) {
        System.out.println("\nLogin Process:");

        System.out.print("Enter your username: ");
        String username = scanner.nextLine();

        System.out.print("Enter your password: ");
        String password = scanner.nextLine();

        if (validateLogin(username, password)) {
            System.out.println("Login successful!");
            manageAccount(scanner, username); // Redirect to account management
        } else {
            System.out.println("Invalid username or password.");
        }
    }

    // Account Management
    public static void manageAccount(Scanner scanner, String username) {
        while (true) {
            System.out.println("\nAccount Management:");
            System.out.println("1. View Account Details");
            System.out.println("2. Edit Account Details");
            System.out.println("3. Logout");

            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    viewAccountDetails(username);
                    break;
                case 2:
                    editAccountDetails(scanner, username);
                    break;
                case 3:
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // View Account Details
    public static void viewAccountDetails(String username) {
        String query = "SELECT id, username, email, contactNumber, birthday, creationDate FROM UserDetails WHERE username = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                System.out.println("\n========== Account Details ==========");
                System.out.printf("ID: %d%n", resultSet.getInt("id"));
                System.out.printf("Username: %s%n", resultSet.getString("username"));
                System.out.printf("Email: %s%n", resultSet.getString("email"));
                System.out.printf("Contact Number: %s%n", resultSet.getString("contactNumber"));
                System.out.printf("Birthday: %s%n", resultSet.getString("birthday"));
                System.out.printf("Account Created On: %s%n", resultSet.getDate("creationDate"));
                System.out.println("=====================================");
            } else {
                System.out.println("No account details found for the given username.");
            }

        } catch (SQLException e) {
            System.out.println("Error fetching account details.");
            e.printStackTrace();
        }
    }


    // Edit Account Details
    public static void editAccountDetails(Scanner scanner, String username) {
        System.out.println("\n========== Edit Account Details ==========");

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement fetchStatement = connection.prepareStatement(
                     "SELECT email, contactNumber, birthday FROM UserDetails WHERE username = ?")) {

            fetchStatement.setString(1, username);
            ResultSet resultSet = fetchStatement.executeQuery();

            if (resultSet.next()) {
                String currentEmail = resultSet.getString("email");
                String currentContactNumber = resultSet.getString("contactNumber");
                String currentBirthday = resultSet.getString("birthday");

                System.out.print("Enter new email (or press Enter to keep current: " + currentEmail + "): ");
                String newEmail = scanner.nextLine().trim();

                System.out.print("Enter new contact number (or press Enter to keep current: " + currentContactNumber + "): ");
                String newContactNumber = scanner.nextLine().trim();

                System.out.print("Enter new birthday (YYYY-MM-DD) (or press Enter to keep current: " + currentBirthday + "): ");
                String newBirthday = scanner.nextLine().trim();

                String query = "UPDATE UserDetails SET email = ?, contactNumber = ?, birthday = ? WHERE username = ?";
                try (PreparedStatement updateStatement = connection.prepareStatement(query)) {
                    updateStatement.setString(1, newEmail.isEmpty() ? currentEmail : newEmail);
                    updateStatement.setString(2, newContactNumber.isEmpty() ? currentContactNumber : newContactNumber);
                    updateStatement.setString(3, newBirthday.isEmpty() ? currentBirthday : newBirthday);
                    updateStatement.setString(4, username);

                    int rowsUpdated = updateStatement.executeUpdate();
                    if (rowsUpdated > 0) {
                        System.out.println("Account details updated successfully!");
                    } else {
                        System.out.println("No changes were made to the account.");
                    }
                }
            } else {
                System.out.println("Error: Account details not found.");
            }

        } catch (SQLException e) {
            System.out.println("An error occurred while editing account details.");
            e.printStackTrace();
        }
    }


    // Helper Methods
    public static boolean registerUserInDatabase(String username, String password, String email, String contactNumber, String birthday) {
        if (isUsernameTaken(username)) {
            return false; // Username already taken
        }

        String query = "INSERT INTO UserDetails (username, password, email, contactNumber, birthday, creationDate) VALUES (?, ?, ?, ?, ?, NOW())";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, email);
            preparedStatement.setString(4, contactNumber);
            preparedStatement.setString(5, birthday);

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Error registering user.");
            e.printStackTrace();
        }
        return false;
    }

    public static boolean validateLogin(String username, String password) {
        String query = "SELECT * FROM UserDetails WHERE username = ? AND password = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();

        } catch (SQLException e) {
            System.out.println("Error validating login.");
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isUsernameTaken(String username) {
        String query = "SELECT * FROM UserDetails WHERE username = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, username);

            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();

        } catch (SQLException e) {
            System.out.println("Error checking username availability.");
            e.printStackTrace();
        }
        return false;
    }
}

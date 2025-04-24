// created on 24/04/2025  updated code uncle added to 
package application;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import javafx.util.Duration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
//import javafx.geometry.Insets;
import javafx.geometry.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;

public class Main extends Application {

    static final String DB_URL = "jdbc:sqlite:C:\\Users\\Jonel\\eclipse-workspace\\Java Testing folder\\TBS testing java\\Taxi.db";

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setScene(createLoginScene(primaryStage));
        primaryStage.setTitle("Login");
        primaryStage.show();
    }

    Scene createLoginScene(Stage primaryStage) {
        Label usernameLabel = new Label("Username");
        TextField usernameField = new TextField();

        Label passwordLabel = new Label("Password");
        PasswordField passwordField = new PasswordField();

        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");

        HBox buttons = new HBox(10, loginButton, registerButton);
        buttons.setStyle("-fx-alignment: center;");

        VBox layout = new VBox(10, usernameLabel, usernameField, passwordLabel, passwordField, buttons);
        layout.setStyle("-fx-padding: 20; -fx-background-color: #ADD8E6;");

        loginButton.setOnAction(e -> checkLogin(usernameField.getText(), passwordField.getText(), primaryStage));
        registerButton.setOnAction(e -> showRegistrationScreen(primaryStage));

        return new Scene(layout, 400, 300);
    }

    private void checkLogin(String username, String password, Stage stage) {
    	
    	String query = "SELECT role FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                switch (role) {
                    case "Admin":
                        showAdminDashboard(stage, username);
                        break;
                    case "Customer":
                        showCustomerDashboard(stage, username);
                        break;
                    case "Driver":
                        showDriverDashboard(stage, username);
                        break;
                    default:
                        showAlert(Alert.AlertType.ERROR, "Invalid role in database.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Invalid username or password.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database error.");
        }
    }


	private void showAdminDashboard(Stage stage, String username) {
        Admin admin = new Admin(stage, username); // Corrected the case of String
        admin.showDashboard(); // Call the showDashboard method to display the dashboard
    }


    private void showDriverDashboard(Stage stage, String username) {
        Driver driver = new Driver(stage, username);
        driver.showDashboard();
    }

    private void showCustomerDashboard(Stage stage, String username) {
        Customer customer = new Customer(stage, username);
        customer.showDashboard();
    }

    private void showRegistrationScreen(Stage stage) {
        Label firstNameLabel = new Label("First Name:");
        TextField firstNameField = new TextField();

        Label lastNameLabel = new Label("Last Name:");
        TextField lastNameField = new TextField();

        Label dobLabel = new Label("DOB:");
        TextField dobField = new TextField();

        Label genderLabel = new Label("Gender:");
        ComboBox<String> genderComboBox = new ComboBox<>();
        genderComboBox.getItems().addAll("Male", "Female", "Other");

        Label addressLabel = new Label("Address:");
        TextField addressField = new TextField();

        Label phoneLabel = new Label("Phone Number:");
        TextField phoneField = new TextField();

        Label emailLabel = new Label("Email Address:");
        TextField emailField = new TextField();

        Label roleLabel = new Label("Role:");
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Admin", "Customer", "Driver");

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();

        Button submitButton = new Button("Submit");

        VBox layout = new VBox(10,
                firstNameLabel, firstNameField,
                lastNameLabel, lastNameField,
                dobLabel, dobField,
                genderLabel, genderComboBox,
                addressLabel, addressField,
                phoneLabel, phoneField,
                emailLabel, emailField,
                roleLabel, roleComboBox,
                usernameLabel, usernameField,
                passwordLabel, passwordField,
                submitButton
        );
        layout.setStyle("-fx-padding: 20px; -fx-background-color: #F0F0F0;");

        submitButton.setOnAction(e -> {
            registerUser(firstNameField.getText(), lastNameField.getText(), dobField.getText(),
                    genderComboBox.getValue(), addressField.getText(), phoneField.getText(),
                    emailField.getText(), roleComboBox.getValue(), usernameField.getText(), passwordField.getText());
        });

        Scene registerScene = new Scene(layout, 400, 600);
        Stage registerStage = new Stage();
        registerStage.setTitle("Register");
        registerStage.setScene(registerScene);
        registerStage.show();
    }

    private void registerUser(String firstName, String lastName, String dob, String gender, String address,
                              String phone, String email, String role, String username, String pwd) {
        String query = "INSERT INTO users (username, password, name, email, phoneNumber, dateOfBirth, gender, address, role) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, username);
            stmt.setString(2, pwd);
            stmt.setString(3, firstName + " " + lastName);
            stmt.setString(4, email);
            stmt.setString(5, phone);
            stmt.setString(6, dob);
            stmt.setString(7, gender);
            stmt.setString(8, address);
            stmt.setString(9, role);

            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int userId = rs.getInt(1);

                    if (role.equals("Admin")) {
                        try (PreparedStatement adminStmt = conn.prepareStatement(
                                "INSERT INTO admin (adminid, name, contactnumber) VALUES (?, ?, ?)")) {
                            adminStmt.setInt(1, userId);
                            adminStmt.setString(2, firstName + " " + lastName);
                            adminStmt.setString(3, phone);
                            adminStmt.executeUpdate();
                        }
                    } else if (role.equals("Driver")) {
                        try (PreparedStatement driverStmt = conn.prepareStatement(
                                "INSERT INTO drivers (driverid, name, phonenumber) VALUES (?, ?, ?)")) {
                            driverStmt.setInt(1, userId);
                            driverStmt.setString(2, firstName + " " + lastName);
                            driverStmt.setString(3, phone);
                            driverStmt.executeUpdate();
                        }
                    }

                    showAlert(Alert.AlertType.INFORMATION, role + " registered successfully.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error during registration.");
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(type.name());
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}





class Admin { 
    private Stage stage;
    private String username;

    public Admin(Stage stage, String username) {
        this.stage = stage;
        this.username = username;
    }

    public void showDashboard() {
        // Left-side navigation buttons
        VBox leftMenu = new VBox(10);
        leftMenu.setPadding(new Insets(20));
        leftMenu.setStyle("-fx-background-color: #D1F2EB;");

        String[] buttonNames = {
            "Assign Driver",
            "Assign Vehicle to Driver",
            "Cancel Booking",
            "Change Password",
            "Logout",
            "Exit"
        };

        for (String name : buttonNames) {
            Button button = new Button(name);
            button.setMinWidth(120);
            button.setStyle("-fx-background-color: #00BCD4; -fx-text-fill: white;");
            if (name.equals("Logout")) {
                button.setOnAction(e -> logout());
            } else if (name.equals("Exit")) {
                button.setOnAction(e -> stage.close());
            } else if (name.equals("Change Password")) {
                button.setOnAction(e -> showChangePasswordDialog());
            }
            leftMenu.getChildren().add(button);
        }

        // Header
        Label welcomeLabel1 = new Label("Welcome " + username);
        welcomeLabel1.setFont(new Font("Arial", 30));
        welcomeLabel1.setTextFill(Color.BLACK);
        welcomeLabel1.setStyle("-fx-background-color: #58D68D; -fx-padding: 20;");
        HBox header = new HBox(welcomeLabel1);
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: #58D68D;");

        // Statistics section
        HBox statsBox = new HBox(50);
        statsBox.setPadding(new Insets(15));
        statsBox.setAlignment(Pos.CENTER);

        Label totalCustomers = new Label("Total Customers: 3");
        Label totalBookings = new Label("Total Bookings: 20");
        Label totalDrivers = new Label("Total Drivers: 6");

        Font statsFont = new Font("Arial", 18);
        totalCustomers.setFont(statsFont);
        totalBookings.setFont(statsFont);
        totalDrivers.setFont(statsFont);

        statsBox.getChildren().addAll(totalCustomers, totalBookings, totalDrivers);

        // Tabs for booking/customer/driver management
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getTabs().addAll(
            createTab("Pending Bookings"),
            createTab("Confirmed Bookings"),
            createTab("Completed Bookings"),
            createCustomerTab(),
            createDriverTab()
        );

        VBox centerContent = new VBox(statsBox, tabPane);
        centerContent.setSpacing(10);
        centerContent.setPadding(new Insets(10));

        // Active Drivers Table (Bottom section)
        Label activeDriversLabel = new Label("Active Drivers");
        activeDriversLabel.setFont(Font.font("Arial", 18));
        activeDriversLabel.setStyle("-fx-font-weight: bold;");

        TableView<String> activeDriversTable = new TableView<>();
        activeDriversTable.setPrefHeight(150);

        TableColumn<String, String> idCol = new TableColumn<>("Driver ID");
        TableColumn<String, String> nameCol = new TableColumn<>("Driver name");
        TableColumn<String, String> phoneCol = new TableColumn<>("Telephone Number");
        TableColumn<String, String> licenseCol = new TableColumn<>("License Number");
        TableColumn<String, String> ratingCol = new TableColumn<>("Rating");

        activeDriversTable.getColumns().addAll(idCol, nameCol, phoneCol, licenseCol, ratingCol);

        // Dummy row example (replace with real data if needed)
        activeDriversTable.getItems().add("add");

        VBox bottomSection = new VBox(activeDriversLabel, activeDriversTable);
        bottomSection.setPadding(new Insets(10));

        // Layout setup
        BorderPane mainLayout = new BorderPane();
        mainLayout.setLeft(leftMenu);
        mainLayout.setTop(header);
        mainLayout.setCenter(centerContent);
        mainLayout.setBottom(bottomSection);

        Scene scene = new Scene(mainLayout, 1200, 700);
        stage.setScene(scene);
        stage.setTitle("Admin Dashboard");
        stage.show();
    }

    private void logout() {
        // Confirmation message before logging out
        showAlert(Alert.AlertType.INFORMATION, "Click OK to log out successfully. Have a good day!");
        // Switch to login screen
        stage.setScene(new Main().createLoginScene(stage));
    }

    private void showChangePasswordDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Change Password");

        Label currentLabel = new Label("Current Password:");
        PasswordField currentField = new PasswordField();

        Label newLabel = new Label("New Password:");
        PasswordField newField = new PasswordField();

        Label confirmLabel = new Label("Confirm Password:");
        PasswordField confirmField = new PasswordField();

        VBox content = new VBox(10,
                currentLabel, currentField,
                newLabel, newField,
                confirmLabel, confirmField
        );
        content.setPadding(new Insets(20));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String currentPwd = currentField.getText();
            String newPwd = newField.getText();
            String confirmPwd = confirmField.getText();

            if (currentPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "All fields are required.");
                event.consume();
                return;
            }

            if (!newPwd.equals(confirmPwd)) {
                showAlert(Alert.AlertType.ERROR, "New password and confirmation do not match.");
                event.consume();
                return;
            }

            try (Connection conn = DriverManager.getConnection(Main.DB_URL);
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT password FROM users WHERE username = ?")) {

                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                if (rs.next() && rs.getString("password").equals(currentPwd)) {
                    try (PreparedStatement updateStmt = conn.prepareStatement(
                            "UPDATE users SET password = ? WHERE username = ?")) {
                        updateStmt.setString(1, newPwd);
                        updateStmt.setString(2, username);
                        updateStmt.executeUpdate();
                        showAlert(Alert.AlertType.INFORMATION, "Password updated successfully.");
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Current password is incorrect.");
                    event.consume();
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database error.");
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(type.name());
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Tab createTab(String name) {
        Tab tab = new Tab(name);

        TableView<String> table = new TableView<>();
        TableColumn<String, String> customerId = new TableColumn<>("Customer ID");
        TableColumn<String, String> customerName = new TableColumn<>("Customer Name");
        TableColumn<String, String> pickup = new TableColumn<>("Pickup Address");
        TableColumn<String, String> drop = new TableColumn<>("Drop Address");
        TableColumn<String, String> date = new TableColumn<>("Pickup Date");
        TableColumn<String, String> time = new TableColumn<>("Pickup Time");

        table.getColumns().addAll(customerId, customerName, pickup, drop, date, time);
        table.getItems().add("data"); // Dummy row for visual layout

        tab.setContent(table);
        return tab;
    }

    private Tab createCustomerTab() {
        Tab tab = new Tab("Customers");

        TableView<String> table = new TableView<>();
        TableColumn<String, String> customerId = new TableColumn<>("Customer ID");
        TableColumn<String, String> customerName = new TableColumn<>("Customer Name");
        TableColumn<String, String> contact = new TableColumn<>("Contact Info");

        table.getColumns().addAll(customerId, customerName, contact);
        table.getItems().add("customer1");

        tab.setContent(table);
        return tab;
    }

    private Tab createDriverTab() {
        Tab tab = new Tab("Drivers");

        TableView<String> table = new TableView<>();
        TableColumn<String, String> driverId = new TableColumn<>("Driver ID");
        TableColumn<String, String> driverName = new TableColumn<>("Driver Name");
        TableColumn<String, String> phone = new TableColumn<>("Phone");
        TableColumn<String, String> license = new TableColumn<>("License");
        TableColumn<String, String> rating = new TableColumn<>("Rating");

        table.getColumns().addAll(driverId, driverName, phone, license, rating);
        table.getItems().add("driver1");

        tab.setContent(table);
        return tab;
    }
}


// === Driver class ===
class Driver {
    private Stage stage;
    private String username;
    private boolean isActive = false;
    private Label statusLabel;

    public Driver(Stage stage, String username) {
        this.stage = stage;
        this.username = username;
    }

    public void showDashboard() {
        Label welcomeLabel = new Label("Welcome " + username);
        welcomeLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: black;");

        HBox topBar = new HBox(welcomeLabel);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(20));
        topBar.setStyle("-fx-background-color: #8BC34A;");

        // === Left Panel ===
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: #E6F9FF;");
        sidebar.setPrefWidth(270);

        statusLabel = new Label("Driver Name, You are Inactive");
        statusLabel.setStyle("-fx-text-fill: red;");

        Button toggleStatusBtn = new Button("Toggle Status");
        toggleStatusBtn.setOnAction(e -> toggleStatus());
        toggleStatusBtn.setStyle("-fx-background-color: black; -fx-text-fill: white;");

        Button changePasswordBtn = new Button("Change Password");
        changePasswordBtn.setStyle("-fx-background-color: #00BCD4; -fx-text-fill: white;");
        changePasswordBtn.setMaxWidth(Double.MAX_VALUE);
        changePasswordBtn.setOnAction(e -> showChangePasswordDialog());

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #00BCD4; -fx-text-fill: white;");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> {
            showAlert(Alert.AlertType.INFORMATION, "Click OK to log out successfully, have a good day!");
            PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
            pause.setOnFinished(event -> stage.setScene(new Main().createLoginScene(stage)));
            pause.play();
        });

        Button exitBtn = new Button("Exit");
        exitBtn.setStyle("-fx-background-color: #00BCD4; -fx-text-fill: white;");
        exitBtn.setMaxWidth(Double.MAX_VALUE);
        exitBtn.setOnAction(e -> stage.close());

        sidebar.getChildren().addAll(
                new Pane(), // space for future image
                new Label("Taxi Booking System"),
                statusLabel,
                toggleStatusBtn,
                changePasswordBtn,
                logoutBtn,
                exitBtn
        );

        // === Assigned Jobs Table ===
        Label jobsLabel = new Label("Assigned Jobs");
        jobsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
        jobsLabel.setAlignment(Pos.CENTER);

        TableView<Object> jobTable = new TableView<>();
        jobTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        jobTable.getColumns().addAll(
                column("Customer Name", "customerName"),
                column("Phone Number", "phoneNumber"),
                column("Pickup Address", "pickupAddress"),
                column("Drop Address", "dropAddress"),
                column("Pickup Date", "pickupDate"),
                column("Pickup Time", "pickupTime"),
                column("Status", "status")
        );

        Button completedBtn = new Button("Completed");
        completedBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        VBox centerBox = new VBox(10, completedBtn, jobsLabel, jobTable);
        centerBox.setPadding(new Insets(20));

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setLeft(sidebar);
        root.setCenter(centerBox);

        Scene scene = new Scene(root, 1200, 700);
        stage.setTitle("Driver Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    private void toggleStatus() {
        isActive = !isActive;
        statusLabel.setText("Driver Name, You are " + (isActive ? "Active" : "Inactive"));
        statusLabel.setStyle("-fx-text-fill: " + (isActive ? "green" : "red") + ";");
    }

    private void showChangePasswordDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Change Password");

        Label currentLabel = new Label("Current Password:");
        PasswordField currentField = new PasswordField();

        Label newLabel = new Label("New Password:");
        PasswordField newField = new PasswordField();

        Label confirmLabel = new Label("Confirm Password:");
        PasswordField confirmField = new PasswordField();

        VBox content = new VBox(10,
                currentLabel, currentField,
                newLabel, newField,
                confirmLabel, confirmField
        );
        content.setPadding(new Insets(20));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String currentPwd = currentField.getText();
            String newPwd = newField.getText();
            String confirmPwd = confirmField.getText();

            if (currentPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "All fields are required.");
                event.consume();
                return;
            }

            if (!newPwd.equals(confirmPwd)) {
                showAlert(Alert.AlertType.ERROR, "New password and confirmation do not match.");
                event.consume();
                return;
            }

            try (Connection conn = DriverManager.getConnection(Main.DB_URL);
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT password FROM users WHERE username = ?")) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                if (rs.next() && rs.getString("password").equals(currentPwd)) {
                    try (PreparedStatement updateStmt = conn.prepareStatement(
                            "UPDATE users SET password = ? WHERE username = ?")) {
                        updateStmt.setString(1, newPwd);
                        updateStmt.setString(2, username);
                        updateStmt.executeUpdate();
                        showAlert(Alert.AlertType.INFORMATION, "Password updated successfully.");
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Current password is incorrect.");
                    event.consume();
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database error.");
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(type.name());
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private <T, Job> TableColumn<Job, T> column(String title, String property) {
        TableColumn<Job, T> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        return col;
    }
}

// === Customer class ===
class Customer {
    private Stage stage;
    private String username;
    private static String selectedPayment;
	public static class Booking {
    private String id;
    private String pickup;
    private String dropoff;
    private String date;
    private String time;
    private String status;
    private String pstatus;
	
   public Booking(String id, String pickup, String dropoff, String date, String time, String status, String pstatus) {
            this.id = id;
            this.pickup = pickup;
            this.dropoff = dropoff;
            this.date = date;
            this.time = time;
            this.status = status;
            this.pstatus = pstatus;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getPickup() { return pickup; }
        public void setPickup(String pickup) { this.pickup = pickup; }

        public String getDropoff() { return dropoff; }
        public void setDropoff(String dropoff) { this.dropoff = dropoff; }
        
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getPStatus() { return pstatus; }
        public void setPStatus(String pstatus) { this.pstatus = pstatus; }
   
}
	
    public Customer(Stage stage, String username) {
        this.stage = stage;
        this.username = username;
        this.loadPendingBookings(username);
    }	
 

    public void showDashboard() {
        Label welcomeLabel = new Label("Welcome " + username);
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: black;");

        HBox topBar = new HBox(welcomeLabel);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(20));
        topBar.setStyle("-fx-background-color: lightgreen;");

        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(10));
        sidebar.setStyle("-fx-background-color: #E6F9FF;");

        Button myProfileBtn = new Button("My Profile");
        Button rateDriverBtn = new Button("Rate Driver");
        Button payCashBtn = new Button("Pay by Cash");
        Button payCardBtn = new Button("Pay by Credit card");
        Button payAppleBtn = new Button("Pay by Apple Pay");
        Button changePwdBtn = new Button("Change Password");
        Button logoutBtn = new Button("Logout");
        Button exitBtn = new Button("Exit");

        for (Button btn : new Button[]{myProfileBtn, rateDriverBtn, changePwdBtn, logoutBtn, exitBtn}) {
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setStyle("-fx-background-color: #00BCD4; -fx-text-fill: white;");
        }
        for (Button btn : new Button[]{payCashBtn, payCardBtn, payAppleBtn}) {
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setStyle("-fx-background-color: orange; -fx-text-fill: black;");
        }

        sidebar.getChildren().addAll(myProfileBtn, rateDriverBtn, payCashBtn, payCardBtn, payAppleBtn,
                changePwdBtn, logoutBtn, exitBtn);

        myProfileBtn.setOnAction(e -> showProfile());

        changePwdBtn.setOnAction(e -> showChangePasswordDialog());
        
        exitBtn.setOnAction(e -> stage.close());

		
        // Add action to the Pay by Cash button
        payCashBtn.setOnAction(e -> {
            showAlert(Alert.AlertType.INFORMATION, "Payment pending. You have chosen to pay in vehicle.");
        });
        
    	// Add action to the Pay by Applepay button
        payAppleBtn.setOnAction(e -> {
            showAlert(Alert.AlertType.INFORMATION, "Payment pending. You have chosen to pay in vehicle.");
        });
        
        payCardBtn.setOnAction(e ->paybycreditcard());
        
        logoutBtn.setOnAction(e -> {
            // Show the success message before logging out
            showAlert(Alert.AlertType.INFORMATION, "Click ok to log out successfully, have a good day!");

            // Create a pause transition (delay of 2 seconds)
            PauseTransition pause = new PauseTransition();

            // After the pause, perform the logout (switch scene to the login screen)
            pause.setOnFinished(event -> {
                stage.setScene(new Main().createLoginScene(stage));
            });

            // Start the pause (delays the action)
            pause.play();
        });

        
        
        ComboBox<String> pickupBox = new ComboBox<>();
        ComboBox<String> dropBox = new ComboBox<>();
        TextField dateField = new TextField();
        TextField timeField = new TextField();

        pickupBox.setPromptText("Pickup Address");
        dropBox.setPromptText("Drop Address");
        dateField.setPromptText("Pickup Date");
        timeField.setPromptText("Pickup Time");

     // Populate the pickupBox and dropBox ComboBox by calling the populateAddressBoxes method
        populateAddressBoxes(pickupBox, dropBox);
        
  
        Button requestBtn = new Button("Request Ride");
        requestBtn.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        Button cancelBtn = new Button("Cancel Booking");
        cancelBtn.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        Button clearBtn = new Button("Clear");
        clearBtn.setStyle("-fx-background-color: orange; -fx-text-fill: black;");

        HBox bookingInputs = new HBox(10, pickupBox, dropBox, dateField, timeField);
        bookingInputs.setPadding(new Insets(10));

        HBox bookingActions = new HBox(10, requestBtn, cancelBtn, clearBtn);
        bookingActions.setPadding(new Insets(10));

        VBox bookingForm = new VBox(10, bookingInputs, bookingActions);

        TabPane tabPane = new TabPane();
        Tab pendingTab = new Tab("Pending Booking");
        Tab historyTab = new Tab("Booking History");
		
		 
		

        TableView<Booking> pendingTable = new TableView<>();
        pendingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        pendingTable.getColumns().addAll(
                column("Booking ID", "id"),
                column("Pickup Address", "pickup"),
                column("Dropoff Address", "dropoff"),
                column("Date", "date"),
                column("Time", "time"),
                column("Status", "status"),
                column("pStatus", "pstatus")
        );

        TableView<Booking> historyTable = new TableView<>();
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        historyTable.getColumns().addAll(
                column("Booking ID", "id"),
                column("Pickup Address", "pickup"),
                column("Dropoff Address", "dropoff"),
                column("Date", "date"),
                column("Time", "time"),
                column("Status", "status"),
                column("pStatus", "pstatus")
        );

        pendingTable.setItems(loadPendingBookings(username));
      /*  pendingTable.setItems(FXCollections.observableArrayList(
                new Booking("2", "London Eye", "Oxford Street", "2025-04-10", "12:30", "Completed","unpaid"),
                new Booking("3", "Hyde Park", "Camden Town", "2025-04-08", "18:45", "Cancelled","unpaid" )
        ));*/

		historyTable.setItems(loadHistoryBookings(username));
       /* historyTable.setItems(FXCollections.observableArrayList(
                new Booking("2", "London Eye", "Oxford Street", "2025-04-10", "12:30", "Completed","unpaid"),
                new Booking("4", "Hyde Park", "Camden Town", "2025-04-08", "18:45", "Cancelled","unpaid" )
        ));*/

        pendingTab.setContent(pendingTable);
        historyTab.setContent(historyTable);
        tabPane.getTabs().addAll(pendingTab, historyTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        VBox mainContent = new VBox(10, bookingForm, tabPane);
        mainContent.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setLeft(sidebar);
        root.setCenter(mainContent);
        requestBtn.setOnAction(e -> {
            String pickup = pickupBox.getValue();
            String dropoff = dropBox.getValue();
            String date = dateField.getText();
            String time = timeField.getText();
            request_ride(pickup, dropoff, date, time);
        });

        
        Scene scene = new Scene(root, 1200, 700);
        stage.setScene(scene);
        stage.setTitle("Customer Dashboard");
        stage.show();
         
        
        
     
    }
    private void request_ride(String pickup, String dropoff, String date, String time) {
        if (pickup == null || pickup.trim().isEmpty() ||
            dropoff == null || dropoff.trim().isEmpty() ||
            date == null || date.trim().isEmpty() ||
            time == null || time.trim().isEmpty()) {
            
            showAlert(Alert.AlertType.WARNING, "All fields must be filled out to request a ride.");
            
            return;
        }

        // You can add logic here to insert the ride request into the database
        //if len(pickup_date)==10 and  pickup_date[2] =='/' and pickup_date[5] =='/':
        if (date.length()==10 && date.charAt(2)=='/' && date.charAt(5)=='/') {
        	//day, month, year = map(int, pickup_date.split('/'))
        	String[] m =date.split("/");
        	//if 1 <= day <= 31 and 1 <= month <= 12 and 2024<=year<=2025 :
        	int day=Integer.parseInt(m[0]);
        	if (1<=day) {
        		
        	}
        	showAlert(Alert.AlertType.INFORMATION, "Ride requested from ");
        }
        showAlert(Alert.AlertType.INFORMATION, "Ride requested from " + pickup + " to " + dropoff + " on " + date + " at " + time);
    }

    private void populateAddressBoxes(ComboBox<String> pickupBox, ComboBox<String> dropBox) {
        // Define a database connection
        try (Connection conn = getConnection()) {
            // SQL query to get all addresses from the 'location' table
            String query = "SELECT address FROM location";
            
            // Prepare the statement
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            // Clear existing items in both ComboBoxes before populating them
            pickupBox.getItems().clear();
            dropBox.getItems().clear();

            // Loop through the result set and add each address to both ComboBoxes
            while (rs.next()) {
                String address = rs.getString("address");  // Get the address from the result set
                pickupBox.getItems().add(address);  // Add address to pickupBox
                dropBox.getItems().add(address);    // Add address to dropBox
            }

        } catch (SQLException e) {
            // Show an alert if an error occurs while fetching data
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error fetching addresses: " + e.getMessage());
        }

        // Update the dropBox when pickupBox value changes to ensure the same address is not selected
        pickupBox.setOnAction(event  -> {
            String selectedPickup = pickupBox.getValue();

            // Define a database connection to get addresses that are not the same as the selected pickup address
            try (Connection conn = getConnection()) {
                // SQL query to get all addresses excluding the selected pickup address
                String query = "SELECT address FROM location WHERE address != ?";
                
                // Prepare the statement
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, selectedPickup);  // Set the pickup address to be excluded from the dropBox
                ResultSet rs = pstmt.executeQuery();

                // Clear dropBox before repopulating it
                dropBox.getItems().clear();

                // Loop through the result set and add each address to dropBox
                while (rs.next()) {
                    String address = rs.getString("address");
                    dropBox.getItems().add(address);  // Add address to dropBox
                }

            } catch (SQLException e) {
                // Show an alert if an error occurs while fetching drop-off addresses
                showAlert(Alert.AlertType.ERROR, "Database Error", "Error fetching dropoff addresses: " + e.getMessage());
            }
        });
    }

	private ObservableList<Booking> loadPendingBookings(String username) {
    ObservableList<Booking> bookings = FXCollections.observableArrayList();
	
 String url = "jdbc:sqlite:C:\\Users\\Jonel\\eclipse-workspace\\Java Testing folder\\TBS testing java\\Taxi.db";
 String sql = "SELECT r.rideid, lp.address, ld.address as address2, r.pickupdate, r.pickuptime, r.status, \r\n"
		 + "               COALESCE(p.paymentstatus, 'Unpaid') as paymentstatus \r\n"
        		
        		+ "        FROM ride r \r\n"
        		+ "        INNER JOIN users u ON r.riderid = u.userid\r\n"
        		+ "        INNER JOIN location lp ON r.pickuplocationid = lp.locationid\r\n"
        		+ "        INNER JOIN location ld ON r.dropofflocationid = ld.locationid\r\n"
        		+ "        LEFT OUTER JOIN payments p ON p.rideid = r.rideid"
				+ "			where r.status='pending'AND u.username=?";

 try (Connection conn = DriverManager.getConnection(url);
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, username);  // Use the username in the query
        ResultSet rs = pstmt.executeQuery();
            
        while (rs.next()) {
            	String rideid = rs.getString("rideid");
                String address = rs.getString("address");
                String address2 = rs.getString("address2");
                String pickupdate = rs.getString("pickupdate");
                String pickuptime = rs.getString("pickuptime");
                String status = rs.getString("status");
                String paymentstatus = rs.getString("paymentstatus");
                bookings.add(new Booking(rideid,address, address2, pickupdate, pickuptime,status,paymentstatus));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    return bookings;
}
private ObservableList<Booking> loadHistoryBookings(String username) {
    ObservableList<Booking> bookings = FXCollections.observableArrayList();
	
 String url = "jdbc:sqlite:C:\\Users\\Jonel\\eclipse-workspace\\Java Testing folder\\TBS testing java\\Taxi.db";
 String sql = "SELECT r.rideid, lp.address, ld.address as address2, r.pickupdate, r.pickuptime, r.status, \r\n"
		 + "               COALESCE(p.paymentstatus, 'Unpaid') as paymentstatus \r\n"
        		
        		+ "        FROM ride r \r\n"
        		+ "        INNER JOIN users u ON r.riderid = u.userid\r\n"
        		+ "        INNER JOIN location lp ON r.pickuplocationid = lp.locationid\r\n"
        		+ "        INNER JOIN location ld ON r.dropofflocationid = ld.locationid\r\n"
        		+ "        LEFT OUTER JOIN payments p ON p.rideid = r.rideid AND u.username=?";

 try (Connection conn = DriverManager.getConnection(url);
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, username);  // Use the username in the query
        ResultSet rs = pstmt.executeQuery();
            
        while (rs.next()) {
            	String rideid = rs.getString("rideid");
                String address = rs.getString("address");
                String address2 = rs.getString("address2");
                String pickupdate = rs.getString("pickupdate");
                String pickuptime = rs.getString("pickuptime");
                String status = rs.getString("status");
                String paymentstatus = rs.getString("paymentstatus");
                bookings.add(new Booking(rideid,address, address2, pickupdate, pickuptime,status,paymentstatus));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    return bookings;
}

// Method to get the database connection
    private Connection getConnection() throws SQLException { 

        return DriverManager.getConnection(Main.DB_URL);
    }

 

 // Helper method to show alerts
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

	private Object paybycreditcard() {
		
    	
    	if (start_payByCreditCard()) {
            showPaymentSuccess();
        }
    	return null;
	}

    public boolean start_payByCreditCard() {
        selectedPayment = "Credit Card";
        
        // Prompt for user inputs
        String cardNumber = getInput("Card Number:");
        String accountNumber = getInput("Account Number:");
        String sortCode = getInput("Sort Code:");
        String expiryDate = getInput("Expiry Date (e.g., MM/YYYY):");

        // Check if any field is missing
        if (cardNumber == null || accountNumber == null || sortCode == null || expiryDate == null) {
            showWarning("Missing Information", "Please fill in all the card details.");
            return false;
        }

        // Validate expiry date
        if (!isValidExpiryDate(expiryDate)) {
            return false;
        }

        return true;
    }

    private String getInput(String prompt) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Input");
        dialog.setHeaderText(prompt);
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private boolean isValidExpiryDate(String expiryDate) throws NumberFormatException {
        try {
            String[] parts = expiryDate.split("/");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Expiry date must be in MM/YYYY format.");
            }

            int expiryMonth = Integer.parseInt(parts[0]);
            int expiryYear = Integer.parseInt(parts[1]);

            LocalDate currentDate = LocalDate.now();
            int currentYear = currentDate.getYear();
            int currentMonth = currentDate.getMonthValue();

            if (expiryYear < currentYear || (expiryYear == currentYear && expiryMonth < currentMonth)) {
                showError("Invalid Expiry Date", "Incorrect expiry year. The card has expired.");
                return false;
            }

        } catch (IllegalArgumentException e) {
            showError("Invalid Expiry Date", "Expiry date must be in MM/YYYY format.");
            return false;
        }
        return true;
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showPaymentSuccess() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Payment Successful");
        alert.setHeaderText(null);
        alert.setContentText("Payment taken successfully with Credit Card.");
        alert.showAndWait();
    }


	// Add the showChangePasswordDialog method here
    private void showChangePasswordDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Change Password");

        Label currentLabel = new Label("Current Password:");
        PasswordField currentField = new PasswordField();

        Label newLabel = new Label("New Password:");
        PasswordField newField = new PasswordField();

        Label confirmLabel = new Label("Confirm Password:");
        PasswordField confirmField = new PasswordField();

        VBox content = new VBox(10,
                currentLabel, currentField,
                newLabel, newField,
                confirmLabel, confirmField
        );
        content.setPadding(new Insets(20));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String currentPwd = currentField.getText();
            String newPwd = newField.getText();
            String confirmPwd = confirmField.getText();

            if (currentPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "All fields are required.");
                event.consume();
                return;
            }

            if (!newPwd.equals(confirmPwd)) {
                showAlert(Alert.AlertType.ERROR, "New password and confirmation do not match.");
                event.consume();
                return;
            }

            try (Connection conn = DriverManager.getConnection(Main.DB_URL);
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT password FROM users WHERE username = ?")) {

                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                if (rs.next() && rs.getString("password").equals(currentPwd)) {
                    try (PreparedStatement updateStmt = conn.prepareStatement(
                            "UPDATE users SET password = ? WHERE username = ?")) {
                        updateStmt.setString(1, newPwd);
                        updateStmt.setString(2, username);
                        updateStmt.executeUpdate();
                        showAlert(Alert.AlertType.INFORMATION, "Password updated successfully.");
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Current password is incorrect.");
                    event.consume();
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database error.");
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    private void showProfile() {
        // SQL query to fetch user details along with payment method, or 'Cash' by default if no completed booking
        String query = "SELECT u.name, u.email, u.phoneNumber, u.dateOfBirth, u.gender, u.address, " +
                       "COALESCE(p.paymentmethod, 'Cash') AS paymentmethod " + // Default to 'Cash' if paymentmethod is null
                       "FROM users u " +
                       "LEFT JOIN ride r ON u.userid = r.riderid " +
                       "LEFT JOIN payments p ON r.rideid = p.rideid AND r.status = 'completed' AND p.paymentstatus = 'complete' " + 
                       "WHERE u.username = ? LIMIT 1";

        try (Connection conn = DriverManager.getConnection(Main.DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username); // Set the username for the query
            
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Fetch user and payment data from the result set
                String name = rs.getString("name");
                String email = rs.getString("email");
                String phone = rs.getString("phoneNumber");
                String dob = rs.getString("dateOfBirth");
                String gender = rs.getString("gender");
                String address = rs.getString("address");
                String paymentMethod = rs.getString("paymentmethod"); // Get payment method from payments table, or 'Cash'

                // Create a dialog to display the user's profile
                Dialog<Void> profileDialog = new Dialog<>();
                profileDialog.setTitle("My Profile");

                VBox profileContent = new VBox(10);
                profileContent.setPadding(new Insets(20));

                // Add user information to the dialog
                profileContent.getChildren().addAll(
                    new Label("Name: " + name),
                    new Label("Email: " + email),
                    new Label("Phone: " + phone),
                    new Label("Date of Birth: " + dob),
                    new Label("Gender: " + gender),
                    new Label("Address: " + address),
                    new Label("Payment Method: " + paymentMethod) // Display the payment method (or default 'Cash')
                );

                profileDialog.getDialogPane().setContent(profileContent);
                profileDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

                // Show the profile dialog
                profileDialog.showAndWait();
            } else {
                showAlert(Alert.AlertType.ERROR, "Profile not found.");
            }

        } catch (SQLException e) {
            // Handle SQL exceptions
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database error while fetching profile: " + e.getMessage());
        }
    }

 
    // Method for showing alerts
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(type.name());
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    
    private <T> TableColumn<Booking, T> column(String title, String property) {
        TableColumn<Booking, T> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        return col;
    }
}


 

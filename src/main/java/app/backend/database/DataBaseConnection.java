package app.backend.database;

/**
 *
 * @author akram, Oday
 */

    
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DataBaseConnection {
    
    // Default connection parameters
    private static final String URL = "jdbc:mysql://localhost:3306/";
    private static final String DATABASE_NAME = "javalearningassistant";
    private static final String USER = "root";
    private static final String PASSWORD = "admin";
    private static Connection connection = null;


    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Load the JDBC driver explicitly
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                // Set additional connection properties
                Properties properties = new Properties();
                properties.setProperty("user", USER);
                properties.setProperty("password", PASSWORD);
                properties.setProperty("useSSL", "false");
                properties.setProperty("allowPublicKeyRetrieval", "true");
                properties.setProperty("serverTimezone", "UTC");
                properties.setProperty("createDatabaseIfNotExist", "true");
                
                // Try to connect with database name
                try {
                    connection = DriverManager.getConnection(URL + DATABASE_NAME, properties);
                    System.out.println("✅ Connected to the database successfully!");
                } catch (SQLException e) {
                    System.out.println("❌ Failed to connect to database: " + e.getMessage());
                    
                    // Try to connect without database name to create it
                    try {
                        connection = DriverManager.getConnection(URL, properties);
                        System.out.println("✅ Connected to MySQL server successfully!");
                        
                        // Create the database if it doesn't exist
                        Statement stmt = connection.createStatement();
                        stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME);
                        stmt.close();
                        
                        // Switch to the new database
                        connection.setCatalog(DATABASE_NAME);
                        System.out.println("✅ Created and switched to database: " + DATABASE_NAME);
                        
                    } catch (SQLException innerEx) {
                        System.out.println("❌ Failed to connect to MySQL server: " + innerEx.getMessage());
                        e.printStackTrace();
                    }
                }
            } catch (ClassNotFoundException e) {
                System.out.println("❌ MySQL JDBC Driver not found. Include it in your library path!");
                e.printStackTrace();
            }
        }
        return connection;
    }

    /**
     * Checks if the database is connected
     * @return true if connected, false otherwise
     */
    public static boolean isDatabaseConnected() {
        if (connection == null) {
            getConnection();
        }
        
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if default valid IDs exist and adds them if they don't
     */
    public static void ensureDefaultValidIDs() {
        if (!isDatabaseConnected()) {
            System.out.println("❌ Cannot initialize default valid IDs: No database connection.");
            return;
        }
        
        try {
            // Check if any validid records exist
            Statement stmt = connection.createStatement();
            java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM validid");
            
            if (rs.next() && rs.getInt("count") == 0) {
                System.out.println("🔄 No valid IDs found. Adding default valid IDs...");
                
                // Add some default valid IDs with the new format
                Statement insertStmt = connection.createStatement();
                insertStmt.executeUpdate("INSERT INTO validid (matricule, role) VALUES ('UNST00000001', 'student')");
                insertStmt.executeUpdate("INSERT INTO validid (matricule, role) VALUES ('UNST00000002', 'student')");
                insertStmt.executeUpdate("INSERT INTO validid (matricule, role) VALUES ('UNTS00000001', 'teacher')");
                insertStmt.executeUpdate("INSERT INTO validid (matricule, role) VALUES ('UNTS00000002', 'teacher')");
                
                System.out.println("✅ Default valid IDs added successfully!");
            } else {
                // Check and add sample IDs with the new format if they don't exist yet
                String[] newIDs = {"UNST00000001", "UNST00000002", "UNTS00000001", "UNTS00000002"};
                String[] roles = {"student", "student", "teacher", "teacher"};
                
                for (int i = 0; i < newIDs.length; i++) {
                    String checkSQL = "SELECT COUNT(*) AS count FROM validid WHERE matricule = ?";
                    PreparedStatement checkStmt = connection.prepareStatement(checkSQL);
                    checkStmt.setString(1, newIDs[i]);
                    ResultSet checkRs = checkStmt.executeQuery();
                    
                    if (checkRs.next() && checkRs.getInt("count") == 0) {
                        System.out.println("🔄 Adding " + newIDs[i] + " to valid IDs...");
                        PreparedStatement insertStmt = connection.prepareStatement(
                            "INSERT INTO validid (matricule, role) VALUES (?, ?)");
                        insertStmt.setString(1, newIDs[i]);
                        insertStmt.setString(2, roles[i]);
                        insertStmt.executeUpdate();
                        System.out.println("✅ " + newIDs[i] + " added successfully!");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error initializing default valid IDs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // method to close the connection
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("🔌 Database connection closed.");
            } catch (SQLException e) {
                System.out.println("❌ Error closing database connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}

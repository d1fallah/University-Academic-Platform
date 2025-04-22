package app.backend.database;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Initializes the database with tables and sample data
 * @author Oday
 */
public class DatabaseInitializer {
    
    /**
     * Initializes the database with tables and default data
     */
    public static void initializeDatabase() {
        System.out.println("Initializing database connection...");
        Connection conn = DataBaseConnection.getConnection();
        
        if (conn == null) {
            System.out.println("❌ Cannot initialize database: Connection failed");
            return;
        }
        
        try {
            // Run SQL script to create tables
            createTablesIfNotExist(conn);
            
            // Add default valid IDs for registration
            DataBaseConnection.ensureDefaultValidIDs();
            
            System.out.println("✅ Database initialization complete");
        } catch (Exception e) {
            System.out.println("❌ Database initialization failed: " + e.getMessage());
            e.printStackTrace();
            System.out.println("⚠️ Database initialization failed or incomplete. Some features may not work properly.");
        }
    }
    
    /**
     * Creates database tables if they don't already exist
     */
    private static void createTablesIfNotExist(Connection conn) {
        try {
            // First check if the user table exists
            boolean tablesExist = tableExists(conn, "user");
            
            if (!tablesExist) {
                System.out.println("🔄 Creating database tables...");
                
                // Load the SQL script from resources
                String sqlScript = loadSqlScript("/app/database/javalearningassistant.sql");
                
                if (sqlScript == null || sqlScript.isEmpty()) {
                    System.out.println("❌ SQL script not found or empty");
                    return;
                }
                
                // Split and execute the script
                executeSqlScript(conn, sqlScript);
                System.out.println("✅ Database tables created successfully");
            } else {
                System.out.println("✅ Database tables already exist");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error creating database tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Checks if a table exists in the database
     */
    private static boolean tableExists(Connection conn, String tableName) {
        try {
            conn.getMetaData().getTables(null, null, tableName, null).next();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Loads SQL script from resources folder
     */
    private static String loadSqlScript(String resourcePath) {
        InputStream is = null;
        try {
            is = DatabaseInitializer.class.getResourceAsStream(resourcePath);
            
            if (is == null) {
                // Try to load from project structure
                is = DatabaseInitializer.class.getClassLoader().getResourceAsStream(resourcePath.substring(1));
            }
            
            if (is == null) {
                // Try a different path
                is = DatabaseInitializer.class.getClassLoader().getResourceAsStream("../java" + resourcePath);
            }
            
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    return reader.lines().collect(Collectors.joining("\n"));
                }
            } else {
                System.out.println("❌ Could not find SQL script: " + resourcePath);
                return null;
            }
        } catch (Exception e) {
            System.out.println("❌ Error loading SQL script: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    // Ignore close exception
                }
            }
        }
    }
    
    /**
     * Executes an SQL script containing multiple statements
     */
    private static void executeSqlScript(Connection conn, String sqlScript) throws SQLException {
        // Split the script into individual statements
        String[] statements = sqlScript.split(";");
        
        try (Statement stmt = conn.createStatement()) {
            for (String statement : statements) {
                String trimmedStmt = statement.trim();
                if (!trimmedStmt.isEmpty() && !trimmedStmt.startsWith("--")) {
                    try {
                        stmt.execute(trimmedStmt);
                    } catch (SQLException e) {
                        // Skip errors for CREATE DATABASE statements and comments
                        if (!trimmedStmt.toUpperCase().contains("CREATE DATABASE") && 
                            !trimmedStmt.toUpperCase().contains("USE") &&
                            !trimmedStmt.startsWith("/*") &&
                            !trimmedStmt.startsWith("SET")) {
                            System.out.println("⚠️ Error executing SQL: " + trimmedStmt);
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
package backend.database;

/**
 *
 * @author akram
 */

    
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class DataBaseConnection {
    

    private static final String URL = "jdbc:mysql://localhost:3306/JavaLearningAssistant";
    private static final String USER = "root";
    private static final String PASSWORD ="";
    private static Connection connection = null;


    public static Connection getConnection(){
        if(connection == null){
            try{
                connection = DriverManager.getConnection(URL,USER, PASSWORD);
                System.out.println("✅ Connected to the database successfully!");
            }   catch(SQLException e){
                    System.out.println("❌ Failed to connect to the database.");
            }
        }
        return connection;
    }

    // method to close the connection

    public static void closeConnection(){
        if(connection != null){
            try {
                connection.close();
                System.out.println("🔌 Database connection closed.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

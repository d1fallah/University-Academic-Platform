package backend.services;

import backend.database.DataBaseConnection;
import backend.models.User;
import backend.utils.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {

    // Sign up a new user
    public static boolean signup(User user) {
        Connection conn = DataBaseConnection.getConnection();
    
        try {
            // Step 1: Check if Matricule is in ValidID table and fetch enrollment level + university name
            String checkMatriculeSQL = "SELECT enrollment_level, university_name FROM ValidID WHERE matricule = ? AND role = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkMatriculeSQL);
            checkStmt.setString(1, user.getMatricule());
            checkStmt.setString(2, user.getRole());
            ResultSet rs = checkStmt.executeQuery();
    
            if (!rs.next()) {
                System.out.println("❌ Invalid Matricule or Role.");
                return false;
            }
    
            // Fetch enrollment level and university from ValidID
            String enrollmentLevel = rs.getString("enrollment_level");
            String universityName = rs.getString("university_name");
    
            // Step 2: Check if Matricule already used in User table
            String existingUserSQL = "SELECT * FROM User WHERE matricule = ?";
            PreparedStatement existStmt = conn.prepareStatement(existingUserSQL);
            existStmt.setString(1, user.getMatricule());
            ResultSet existRs = existStmt.executeQuery();
    
            if (existRs.next()) {
                System.out.println("❌ Matricule already used.");
                return false;
            }
    
            // Step 3: Insert new user with fetched enrollment level and university name
            String insertSQL = "INSERT INTO User (name, password, matricule, role, enrollment_level, university_name) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSQL);
            insertStmt.setString(1, user.getName());
            insertStmt.setString(2, PasswordHasher.hashPassword(user.getPassword()));
            insertStmt.setString(3, user.getMatricule());
            insertStmt.setString(4, user.getRole());
            insertStmt.setString(5, enrollmentLevel);
            insertStmt.setString(6, universityName);
    
            int rowsInserted = insertStmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("✅ Signup successful!");
                return true;
            } else {
                System.out.println("❌ Signup failed.");
                return false;
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    

    // Login user
    public static User login(String matricule, String password) {
        Connection conn = DataBaseConnection.getConnection();

        try {
            String sql = "SELECT * FROM User WHERE matricule = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, matricule);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                if (PasswordHasher.checkPassword(password, storedPassword)) {
                    User user = new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        storedPassword,
                        rs.getString("matricule"),
                        rs.getString("role"),
                        rs.getTimestamp("created_at"),
                        rs.getString("enrollment_level"),
                        rs.getString("university_name")
                    );
                    System.out.println("✅ Login successful. Welcome " + user.getName() + "!");
                    return user;
                } else {
                    System.out.println("❌ Incorrect password.");
                }
            } else {
                System.out.println("❌ Matricule not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}

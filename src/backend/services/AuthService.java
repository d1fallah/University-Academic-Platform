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
            // Step 1: Check if Matricule is in ValidID table
            String checkMatriculeSQL = "SELECT * FROM ValidID WHERE matricule = ? AND role = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkMatriculeSQL);
            checkStmt.setString(1, user.getMatricule());
            checkStmt.setString(2, user.getRole());
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                System.out.println("❌ Invalid Matricule or Role.");
                return false;
            }

            // Step 2: Check if Matricule already used in User table
            String existingUserSQL = "SELECT * FROM User WHERE matricule = ?";
            PreparedStatement existStmt = conn.prepareStatement(existingUserSQL);
            existStmt.setString(1, user.getMatricule());
            ResultSet existRs = existStmt.executeQuery();

            if (existRs.next()) {
                System.out.println("❌ Matricule already used.");
                return false;
            }

            // Step 3: Insert new user
            String insertSQL = "INSERT INTO User (name, password, matricule, role) VALUES (?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSQL);
            insertStmt.setString(1, user.getName());
            insertStmt.setString(2, PasswordHasher.hashPassword(user.getPassword()));
            insertStmt.setString(3, user.getMatricule());
            insertStmt.setString(4, user.getRole());

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
                if (PasswordHasher.checkPassword(password, storedPassword)) { // For now simple, later use PasswordHasher
                    User user = new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        storedPassword,
                        rs.getString("matricule"),
                        rs.getString("role"),
                        rs.getTimestamp("created_at")
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

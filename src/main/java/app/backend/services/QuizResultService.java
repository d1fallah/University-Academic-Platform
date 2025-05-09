package app.backend.services;

import app.backend.database.DataBaseConnection;
import app.backend.models.QuizResult;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizResultService {

    // Submit a quiz result
    public static boolean submitQuizResult(QuizResult quizResult) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "INSERT INTO QuizResult (quiz_id, student_id, score, is_completed) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quizResult.getQuizId());
            stmt.setInt(2, quizResult.getStudentId());
            stmt.setInt(3, quizResult.getScore());
            stmt.setBoolean(4, quizResult.isCompleted());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Check if a student has already taken a quiz
    public static boolean hasStudentTakenQuiz(int studentId, int quizId) {
        Connection conn = DataBaseConnection.getConnection();
        
        String sql = "SELECT COUNT(*) FROM QuizResult WHERE student_id = ? AND quiz_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, quizId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Get a specific quiz result for a student
    public static QuizResult getQuizResult(int studentId, int quizId) {
        Connection conn = DataBaseConnection.getConnection();
        
        String sql = "SELECT * FROM QuizResult WHERE student_id = ? AND quiz_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, quizId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new QuizResult(
                    rs.getInt("id"),
                    rs.getInt("quiz_id"),
                    rs.getInt("student_id"),
                    rs.getInt("score"),
                    rs.getTimestamp("submitted_at"),
                    rs.getBoolean("is_completed")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    // Get all results of a student
    public static List<QuizResult> getResultsByStudentId(int studentId) {
        Connection conn = DataBaseConnection.getConnection();
        List<QuizResult> results = new ArrayList<>();

        String sql = "SELECT * FROM QuizResult WHERE student_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                QuizResult result = new QuizResult(
                    rs.getInt("id"),
                    rs.getInt("quiz_id"),
                    rs.getInt("student_id"),
                    rs.getInt("score"),
                    rs.getTimestamp("submitted_at"),
                    rs.getBoolean("is_completed")
                );
                results.add(result);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    // Get all results of a quiz (for teacher to check all student scores)
    public static List<QuizResult> getResultsByQuizId(int quizId) {
        Connection conn = DataBaseConnection.getConnection();
        List<QuizResult> results = new ArrayList<>();

        String sql = "SELECT * FROM QuizResult WHERE quiz_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quizId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                QuizResult result = new QuizResult(
                    rs.getInt("id"),
                    rs.getInt("quiz_id"),
                    rs.getInt("student_id"),
                    rs.getInt("score"),
                    rs.getTimestamp("submitted_at"),
                    rs.getBoolean("is_completed")
                );
                results.add(result);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }
}

package app.backend.services;

import app.backend.database.DataBaseConnection;
import app.backend.models.Quiz;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizService {

    // Add a new quiz
    public static boolean addQuiz(Quiz quiz) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "INSERT INTO Quiz (course_id, title, description, comment) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quiz.getCourseId());
            stmt.setString(2, quiz.getTitle());
            stmt.setString(3, quiz.getDescription());
            stmt.setString(4, quiz.getComment());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update an existing quiz
    public static boolean updateQuiz(Quiz quiz) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "UPDATE Quiz SET title = ?, description = ?, comment = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, quiz.getTitle());
            stmt.setString(2, quiz.getDescription());
            stmt.setString(3, quiz.getComment());
            stmt.setInt(4, quiz.getId());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete a quiz
    public static boolean deleteQuiz(int quizId) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "DELETE FROM Quiz WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quizId);

            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all quizzes for a course
    public static List<Quiz> getQuizzesByCourseId(int courseId) {
        Connection conn = DataBaseConnection.getConnection();
        List<Quiz> quizzes = new ArrayList<>();

        String sql = "SELECT * FROM Quiz WHERE course_id = ? ORDER BY created_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Quiz quiz = new Quiz(
                    rs.getInt("id"),
                    rs.getInt("course_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("comment"),
                    rs.getTimestamp("created_at")
                );
                quizzes.add(quiz);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return quizzes;
    }

    // Get a quiz by ID
    public static Quiz getQuizById(int quizId) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "SELECT * FROM Quiz WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quizId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Quiz(
                    rs.getInt("id"),
                    rs.getInt("course_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("comment"),
                    rs.getTimestamp("created_at")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}

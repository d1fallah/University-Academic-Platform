package app.backend.services;

import app.backend.database.DataBaseConnection;
import app.backend.models.Question;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionService {

    // Add a new question
    public static boolean addQuestion(Question question) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "INSERT INTO Question (quiz_id, question_text) VALUES (?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, question.getQuizId());
            stmt.setString(2, question.getQuestionText());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update an existing question
    public static boolean updateQuestion(Question question) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "UPDATE Question SET question_text = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, question.getQuestionText());
            stmt.setInt(2, question.getId());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete a question
    public static boolean deleteQuestion(int questionId) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "DELETE FROM Question WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, questionId);

            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all questions for a quiz
    public static List<Question> getQuestionsByQuizId(int quizId) {
        Connection conn = DataBaseConnection.getConnection();
        List<Question> questions = new ArrayList<>();

        String sql = "SELECT * FROM Question WHERE quiz_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quizId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Question question = new Question(
                    rs.getInt("id"),
                    rs.getInt("quiz_id"),
                    rs.getString("question_text")
                );
                questions.add(question);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return questions;
    }

    // Get a question by ID
    public static Question getQuestionById(int questionId) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "SELECT * FROM Question WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, questionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Question(
                    rs.getInt("id"),
                    rs.getInt("quiz_id"),
                    rs.getString("question_text")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}

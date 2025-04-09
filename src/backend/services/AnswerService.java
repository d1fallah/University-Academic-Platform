package backend.services;

import backend.database.DataBaseConnection;
import backend.models.Answer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AnswerService {

    // Add a new answer
    public static boolean addAnswer(Answer answer) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "INSERT INTO Answer (question_id, answer_text, is_correct) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, answer.getQuestionId());
            stmt.setString(2, answer.getAnswerText());
            stmt.setBoolean(3, answer.isCorrect());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update an existing answer
    public static boolean updateAnswer(Answer answer) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "UPDATE Answer SET answer_text = ?, is_correct = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, answer.getAnswerText());
            stmt.setBoolean(2, answer.isCorrect());
            stmt.setInt(3, answer.getId());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete an answer
    public static boolean deleteAnswer(int answerId) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "DELETE FROM Answer WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, answerId);

            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all answers for a question
    public static List<Answer> getAnswersByQuestionId(int questionId) {
        Connection conn = DataBaseConnection.getConnection();
        List<Answer> answers = new ArrayList<>();

        String sql = "SELECT * FROM Answer WHERE question_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, questionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Answer answer = new Answer(
                    rs.getInt("id"),
                    rs.getInt("question_id"),
                    rs.getString("answer_text"),
                    rs.getBoolean("is_correct")
                );
                answers.add(answer);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return answers;
    }

    // Get an answer by ID
    public static Answer getAnswerById(int answerId) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "SELECT * FROM Answer WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, answerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Answer(
                    rs.getInt("id"),
                    rs.getInt("question_id"),
                    rs.getString("answer_text"),
                    rs.getBoolean("is_correct")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}

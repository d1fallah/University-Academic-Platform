package app.backend.services;

import app.backend.database.DataBaseConnection;
import app.backend.models.StudentAnswer;
import app.backend.models.Answer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentAnswerService {

    // Save a student's answer
    public static boolean saveStudentAnswer(StudentAnswer studentAnswer) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "INSERT INTO studentanswer (quiz_result_id, question_id, selected_answer_id, is_correct) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentAnswer.getQuizResultId());
            stmt.setInt(2, studentAnswer.getQuestionId());
            
            if (studentAnswer.getSelectedAnswerId() != null) {
                stmt.setInt(3, studentAnswer.getSelectedAnswerId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            
            stmt.setBoolean(4, studentAnswer.isCorrect());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Save multiple student answers at once
    public static boolean saveStudentAnswers(List<StudentAnswer> studentAnswers) {
        Connection conn = DataBaseConnection.getConnection();
        boolean success = true;

        try {
            conn.setAutoCommit(false);

            String sql = "INSERT INTO studentanswer (quiz_result_id, question_id, selected_answer_id, is_correct) VALUES (?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (StudentAnswer answer : studentAnswers) {
                    stmt.setInt(1, answer.getQuizResultId());
                    stmt.setInt(2, answer.getQuestionId());
                    
                    if (answer.getSelectedAnswerId() != null) {
                        stmt.setInt(3, answer.getSelectedAnswerId());
                    } else {
                        stmt.setNull(3, Types.INTEGER);
                    }
                    
                    stmt.setBoolean(4, answer.isCorrect());
                    stmt.addBatch();
                }

                int[] results = stmt.executeBatch();
                
                // Check if all insertions were successful
                for (int result : results) {
                    if (result <= 0) {
                        success = false;
                        break;
                    }
                }
            }

            if (success) {
                conn.commit();
            } else {
                conn.rollback();
            }

            conn.setAutoCommit(true);
            return success;

        } catch (SQLException e) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
    }

    // Get all answers for a specific quiz result
    public static List<StudentAnswer> getStudentAnswers(int quizResultId) {
        Connection conn = DataBaseConnection.getConnection();
        List<StudentAnswer> answers = new ArrayList<>();

        String sql = "SELECT * FROM studentanswer WHERE quiz_result_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quizResultId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                StudentAnswer answer = new StudentAnswer(
                    rs.getInt("id"),
                    rs.getInt("quiz_result_id"),
                    rs.getInt("question_id"),
                    rs.getObject("selected_answer_id") != null ? rs.getInt("selected_answer_id") : null,
                    rs.getBoolean("is_correct")
                );
                answers.add(answer);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return answers;
    }
} 
package backend.services;

import backend.database.DataBaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StatisticsService {

    // Get number of students registered
    public static int getTotalStudents() {
        Connection conn = DataBaseConnection.getConnection();
        String sql = "SELECT COUNT(*) FROM User WHERE role = 'student'";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Get number of students who submitted an exercise
    public static int getExerciseSubmissionCount(int exerciseId) {
        Connection conn = DataBaseConnection.getConnection();
        String sql = "SELECT COUNT(DISTINCT student_id) FROM ExerciseSubmission WHERE exercise_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, exerciseId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Get number of students who submitted a practical work
    public static int getPracticalWorkSubmissionCount(int practicalWorkId) {
        Connection conn = DataBaseConnection.getConnection();
        String sql = "SELECT COUNT(DISTINCT student_id) FROM PracticalWorkSubmission WHERE practical_work_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, practicalWorkId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Get number of students who submitted a quiz result
    public static int getQuizResultSubmissionCount(int quizId) {
        Connection conn = DataBaseConnection.getConnection();
        String sql = "SELECT COUNT(DISTINCT student_id) FROM QuizResult WHERE quiz_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quizId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Calculate progress percentage
    public static double calculateProgress(int submissionCount, int totalStudents) {
        if (totalStudents == 0) return 0.0;
        return (submissionCount * 100.0) / totalStudents;
    }
}

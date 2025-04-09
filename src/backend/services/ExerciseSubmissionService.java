package backend.services;

import backend.database.DataBaseConnection;
import backend.models.ExerciseSubmission;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExerciseSubmissionService {

    // Submit an exercise
    public static boolean submitExercise(ExerciseSubmission submission) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "INSERT INTO ExerciseSubmission (exercise_id, student_id, submission_text) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, submission.getExerciseId());
            stmt.setInt(2, submission.getStudentId());
            stmt.setString(3, submission.getSubmissionText());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all submissions for a specific exercise
    public static List<ExerciseSubmission> getSubmissionsByExerciseId(int exerciseId) {
        Connection conn = DataBaseConnection.getConnection();
        List<ExerciseSubmission> submissions = new ArrayList<>();

        String sql = "SELECT * FROM ExerciseSubmission WHERE exercise_id = ? ORDER BY submitted_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, exerciseId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ExerciseSubmission submission = new ExerciseSubmission(
                    rs.getInt("id"),
                    rs.getInt("exercise_id"),
                    rs.getInt("student_id"),
                    rs.getString("submission_text"),
                    rs.getTimestamp("submitted_at")
                );
                submissions.add(submission);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return submissions;
    }

    // Get all submissions made by a student
    public static List<ExerciseSubmission> getSubmissionsByStudentId(int studentId) {
        Connection conn = DataBaseConnection.getConnection();
        List<ExerciseSubmission> submissions = new ArrayList<>();

        String sql = "SELECT * FROM ExerciseSubmission WHERE student_id = ? ORDER BY submitted_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ExerciseSubmission submission = new ExerciseSubmission(
                    rs.getInt("id"),
                    rs.getInt("exercise_id"),
                    rs.getInt("student_id"),
                    rs.getString("submission_text"),
                    rs.getTimestamp("submitted_at")
                );
                submissions.add(submission);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return submissions;
    }
}

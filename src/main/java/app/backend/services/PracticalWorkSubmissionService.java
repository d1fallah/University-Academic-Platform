package app.backend.services;

import app.backend.database.DataBaseConnection;
import app.backend.models.PracticalWorkSubmission;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PracticalWorkSubmissionService {

    // Submit a practical work
    public static boolean submitPracticalWork(PracticalWorkSubmission submission) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "INSERT INTO PracticalWorkSubmission (practical_work_id, student_id, file_path) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, submission.getPracticalWorkId());
            stmt.setInt(2, submission.getStudentId());
            stmt.setString(3, submission.getFilePath());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all submissions for a practical work
    public static List<PracticalWorkSubmission> getSubmissionsByPracticalWorkId(int practicalWorkId) {
        Connection conn = DataBaseConnection.getConnection();
        List<PracticalWorkSubmission> submissions = new ArrayList<>();

        String sql = "SELECT * FROM PracticalWorkSubmission WHERE practical_work_id = ? ORDER BY submitted_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, practicalWorkId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                PracticalWorkSubmission submission = new PracticalWorkSubmission(
                    rs.getInt("id"),
                    rs.getInt("practical_work_id"),
                    rs.getInt("student_id"),
                    rs.getString("file_path"),
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
    public static List<PracticalWorkSubmission> getSubmissionsByStudentId(int studentId) {
        Connection conn = DataBaseConnection.getConnection();
        List<PracticalWorkSubmission> submissions = new ArrayList<>();

        String sql = "SELECT * FROM PracticalWorkSubmission WHERE student_id = ? ORDER BY submitted_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                PracticalWorkSubmission submission = new PracticalWorkSubmission(
                    rs.getInt("id"),
                    rs.getInt("practical_work_id"),
                    rs.getInt("student_id"),
                    rs.getString("file_path"),
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

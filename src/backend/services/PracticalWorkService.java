package backend.services;

import backend.database.DataBaseConnection;
import backend.models.PracticalWork;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PracticalWorkService {

    // Add a new practical work
    public static boolean addPracticalWork(PracticalWork practicalWork) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "INSERT INTO PracticalWork (course_id, title, description, comment, deadline) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, practicalWork.getCourseId());
            stmt.setString(2, practicalWork.getTitle());
            stmt.setString(3, practicalWork.getDescription());
            stmt.setString(4, practicalWork.getComment());
            stmt.setDate(5, practicalWork.getDeadline());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update an existing practical work
    public static boolean updatePracticalWork(PracticalWork practicalWork) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "UPDATE PracticalWork SET title = ?, description = ?, comment = ?, deadline = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, practicalWork.getTitle());
            stmt.setString(2, practicalWork.getDescription());
            stmt.setString(3, practicalWork.getComment());
            stmt.setDate(4, practicalWork.getDeadline());
            stmt.setInt(5, practicalWork.getId());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete a practical work
    public static boolean deletePracticalWork(int practicalWorkId) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "DELETE FROM PracticalWork WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, practicalWorkId);

            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all practical works for a course
    public static List<PracticalWork> getPracticalWorksByCourseId(int courseId) {
        Connection conn = DataBaseConnection.getConnection();
        List<PracticalWork> works = new ArrayList<>();

        String sql = "SELECT * FROM PracticalWork WHERE course_id = ? ORDER BY created_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                PracticalWork work = new PracticalWork(
                    rs.getInt("id"),
                    rs.getInt("course_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("comment"),
                    rs.getDate("deadline"),
                    rs.getTimestamp("created_at")
                );
                works.add(work);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return works;
    }

    // Get a single practical work by ID
    public static PracticalWork getPracticalWorkById(int practicalWorkId) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "SELECT * FROM PracticalWork WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, practicalWorkId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new PracticalWork(
                    rs.getInt("id"),
                    rs.getInt("course_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("comment"),
                    rs.getDate("deadline"),
                    rs.getTimestamp("created_at")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}

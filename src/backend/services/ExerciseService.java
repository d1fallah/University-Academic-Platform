package backend.services;

import backend.database.DataBaseConnection;
import backend.models.Exercise;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExerciseService {

    // Add a new exercise
    public static boolean addExercise(Exercise exercise) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "INSERT INTO Exercise (course_id, title, description, comment) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, exercise.getCourseId());
            stmt.setString(2, exercise.getTitle());
            stmt.setString(3, exercise.getDescription());
            stmt.setString(4, exercise.getComment());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update an existing exercise
    public static boolean updateExercise(Exercise exercise) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "UPDATE Exercise SET title = ?, description = ?, comment = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, exercise.getTitle());
            stmt.setString(2, exercise.getDescription());
            stmt.setString(3, exercise.getComment());
            stmt.setInt(4, exercise.getId());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete an exercise
    public static boolean deleteExercise(int exerciseId) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "DELETE FROM Exercise WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, exerciseId);

            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all exercises for a course
    public static List<Exercise> getExercisesByCourseId(int courseId) {
        Connection conn = DataBaseConnection.getConnection();
        List<Exercise> exercises = new ArrayList<>();

        String sql = "SELECT * FROM Exercise WHERE course_id = ? ORDER BY created_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Exercise exercise = new Exercise(
                    rs.getInt("id"),
                    rs.getInt("course_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("comment"),
                    rs.getTimestamp("created_at")
                );
                exercises.add(exercise);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return exercises;
    }

    // Get a single exercise by ID
    public static Exercise getExerciseById(int exerciseId) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "SELECT * FROM Exercise WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, exerciseId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Exercise(
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

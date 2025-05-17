package app.backend.services;

import app.backend.database.DataBaseConnection;
import app.backend.models.Exercise;
import app.backend.models.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExerciseService {

    // Add a new exercise
    public static boolean addExercise(Exercise exercise) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "INSERT INTO exercice (course_id, title, description, comment, pdf_path, target_level, teacher_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, exercise.getCourseId());
            stmt.setString(2, exercise.getTitle());
            stmt.setString(3, exercise.getDescription());
            stmt.setString(4, exercise.getComment());
            stmt.setString(5, exercise.getPdfPath());
            stmt.setString(6, exercise.getTargetLevel());
            stmt.setInt(7, exercise.getTeacherId());

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

        String sql = "UPDATE exercice SET title = ?, description = ?, comment = ?, course_id = ?, pdf_path = ?, target_level = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, exercise.getTitle());
            stmt.setString(2, exercise.getDescription());
            stmt.setString(3, exercise.getComment());
            stmt.setInt(4, exercise.getCourseId());
            stmt.setString(5, exercise.getPdfPath());
            stmt.setString(6, exercise.getTargetLevel());
            stmt.setInt(7, exercise.getId());

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

        String sql = "DELETE FROM exercice WHERE id = ?";

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

        String sql = "SELECT * FROM exercice WHERE course_id = ? ORDER BY created_at DESC";

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
                    rs.getTimestamp("created_at"),
                    rs.getString("pdf_path"),
                    rs.getString("target_level"),
                    rs.getInt("teacher_id")
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

        String sql = "SELECT * FROM exercice WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, exerciseId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Exercise exercise = new Exercise(
                    rs.getInt("id"),
                    rs.getInt("course_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("comment"),
                    rs.getTimestamp("created_at"),
                    rs.getString("pdf_path"),
                    rs.getString("target_level"),
                    rs.getInt("teacher_id")
                );
                return exercise;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    
    // Get all exercises by teacher ID
    public static List<Exercise> getExercisesByTeacherId(int teacherId) {
        Connection conn = DataBaseConnection.getConnection();
        List<Exercise> exercises = new ArrayList<>();

        String sql = "SELECT * FROM exercice WHERE teacher_id = ? ORDER BY created_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Exercise exercise = new Exercise(
                    rs.getInt("id"),
                    rs.getInt("course_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("comment"),
                    rs.getTimestamp("created_at"),
                    rs.getString("pdf_path"),
                    rs.getString("target_level"),
                    rs.getInt("teacher_id")
                );
                exercises.add(exercise);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return exercises;
    }
    
    // Get all teachers who have published exercises
    public static List<User> getTeachersWithExercises() {
        Connection conn = DataBaseConnection.getConnection();
        List<User> teachers = new ArrayList<>();

        String sql = "SELECT DISTINCT u.* FROM User u " +
                    "JOIN exercice e ON u.id = e.teacher_id " +
                    "WHERE u.role = 'teacher' " +
                    "ORDER BY u.name";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                User teacher = new User();
                teacher.setId(rs.getInt("id"));
                teacher.setName(rs.getString("name"));
                teacher.setMatricule(rs.getString("matricule"));
                teacher.setRole(rs.getString("role"));
                teacher.setEnrollmentLevel(rs.getString("enrollment_level"));
                teacher.setUniversityName(rs.getString("university_name"));
                
                teachers.add(teacher);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return teachers;
    }
    
    // Get teachers with exercises filtered by student level
    public static List<User> getTeachersWithExercisesByLevel(String studentLevel) {
        Connection conn = DataBaseConnection.getConnection();
        List<User> teachers = new ArrayList<>();

        String sql = "SELECT DISTINCT u.* FROM User u " +
                    "JOIN exercice e ON u.id = e.teacher_id " +
                    "WHERE u.role = 'teacher' " +
                    "AND (e.target_level = ? OR e.target_level IS NULL) " +
                    "ORDER BY u.name";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentLevel);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                User teacher = new User();
                teacher.setId(rs.getInt("id"));
                teacher.setName(rs.getString("name"));
                teacher.setMatricule(rs.getString("matricule"));
                teacher.setRole(rs.getString("role"));
                teacher.setEnrollmentLevel(rs.getString("enrollment_level"));
                teacher.setUniversityName(rs.getString("university_name"));
                
                teachers.add(teacher);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return teachers;
    }
    
    // Get exercises by teacher ID and student level
    public static List<Exercise> getExercisesByTeacherAndLevel(int teacherId, String studentLevel) {
        Connection conn = DataBaseConnection.getConnection();
        List<Exercise> exercises = new ArrayList<>();

        String sql = "SELECT * FROM exercice " +
                    "WHERE teacher_id = ? " +
                    "AND (target_level = ? OR target_level IS NULL) " +
                    "ORDER BY created_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            stmt.setString(2, studentLevel);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Exercise exercise = new Exercise(
                    rs.getInt("id"),
                    rs.getInt("course_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("comment"),
                    rs.getTimestamp("created_at"),
                    rs.getString("pdf_path"),
                    rs.getString("target_level"),
                    rs.getInt("teacher_id")
                );
                exercises.add(exercise);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return exercises;
    }
}

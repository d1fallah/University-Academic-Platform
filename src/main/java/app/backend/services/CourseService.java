package app.backend.services;

import app.backend.database.DataBaseConnection;
import app.backend.models.Course;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseService {

    // Add a new course
    public static boolean addCourse(Course course) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "INSERT INTO Course (title, description, comment, teacher_id) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, course.getTitle());
            stmt.setString(2, course.getDescription());
            stmt.setString(3, course.getComment());
            stmt.setInt(4, course.getTeacherId());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update an existing course
    public static boolean updateCourse(Course course) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "UPDATE Course SET title = ?, description = ?, comment = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, course.getTitle());
            stmt.setString(2, course.getDescription());
            stmt.setString(3, course.getComment());
            stmt.setInt(4, course.getId());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete a course
    public static boolean deleteCourse(int courseId) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "DELETE FROM Course WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);

            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all courses
    public static List<Course> getAllCourses() {
        Connection conn = DataBaseConnection.getConnection();
        List<Course> courses = new ArrayList<>();

        String sql = "SELECT * FROM Course ORDER BY created_at DESC";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Course course = new Course(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("comment"),
                    rs.getInt("teacher_id"),
                    rs.getTimestamp("created_at")
                );
                courses.add(course);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return courses;
    }

    // Get course by ID
    public static Course getCourseById(int courseId) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "SELECT * FROM Course WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Course(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("comment"),
                    rs.getInt("teacher_id"),
                    rs.getTimestamp("created_at")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    
    // Get courses by teacher ID
    public static List<Course> getCoursesByTeacherId(int teacherId) {
        Connection conn = DataBaseConnection.getConnection();
        List<Course> courses = new ArrayList<>();

        String sql = "SELECT * FROM Course WHERE teacher_id = ? ORDER BY created_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Course course = new Course(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("comment"),
                    rs.getInt("teacher_id"),
                    rs.getTimestamp("created_at")
                );
                courses.add(course);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return courses;
    }
}

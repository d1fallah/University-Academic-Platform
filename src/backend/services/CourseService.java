package backend.services;

import backend.database.DataBaseConnection;
import backend.models.Course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseService {

    // Add a new course
    public static boolean addCourse(Course course) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "INSERT INTO Course (title, description, comment, pdf_path, teacher_id, target_level) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, course.getTitle());
            stmt.setString(2, course.getDescription());
            stmt.setString(3, course.getComment());
            stmt.setString(4, course.getPdfPath());
            stmt.setInt(5, course.getTeacherId());
            stmt.setString(6, course.getTargetLevel());

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

        String sql = "UPDATE Course SET title = ?, description = ?, comment = ?, pdf_path = ?, target_level = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, course.getTitle());
            stmt.setString(2, course.getDescription());
            stmt.setString(3, course.getComment());
            stmt.setString(4, course.getPdfPath());
            stmt.setString(5, course.getTargetLevel());
            stmt.setInt(6, course.getId());

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
                    rs.getString("pdf_path"),
                    rs.getInt("teacher_id"),
                    rs.getTimestamp("created_at"),
                    rs.getString("target_level")
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
                    rs.getString("pdf_path"),
                    rs.getInt("teacher_id"),
                    rs.getTimestamp("created_at"),
                    rs.getString("target_level")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // ✅ Get courses only for a student level (L1, L2, etc.)
    public static List<Course> getCoursesByStudentLevel(String level) {
        Connection conn = DataBaseConnection.getConnection();
        List<Course> courses = new ArrayList<>();

        String sql = "SELECT * FROM Course WHERE target_level = ? ORDER BY created_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, level);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Course course = new Course(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("comment"),
                    rs.getString("pdf_path"),
                    rs.getInt("teacher_id"),
                    rs.getTimestamp("created_at"),
                    rs.getString("target_level")
                );
                courses.add(course);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return courses;
    }
}

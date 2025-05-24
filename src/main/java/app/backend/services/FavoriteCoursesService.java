package app.backend.services;

import app.backend.database.DataBaseConnection;
import app.backend.models.Course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavoriteCoursesService {
    
    /**
     * Add a course to student's favorites
     * 
     * @param studentId The ID of the student
     * @param courseId The ID of the course to favorite
     * @return boolean indicating success
     */
    public static boolean addFavoriteCourse(int studentId, int courseId) {
        Connection conn = DataBaseConnection.getConnection();
        String sql = "INSERT INTO favorite_courses (student_id, course_id) VALUES (?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, courseId);
            
            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Remove a course from student's favorites
     * 
     * @param studentId The ID of the student
     * @param courseId The ID of the course to unfavorite
     * @return boolean indicating success
     */
    public static boolean removeFavoriteCourse(int studentId, int courseId) {
        Connection conn = DataBaseConnection.getConnection();
        String sql = "DELETE FROM favorite_courses WHERE student_id = ? AND course_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, courseId);
            
            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Check if a course is in student's favorites
     * 
     * @param studentId The ID of the student
     * @param courseId The ID of the course to check
     * @return boolean indicating if course is favorited
     */
    public static boolean isFavoriteCourse(int studentId, int courseId) {
        Connection conn = DataBaseConnection.getConnection();
        String sql = "SELECT 1 FROM favorite_courses WHERE student_id = ? AND course_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, courseId);
            
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get all favorite courses for a student
     * 
     * @param studentId The ID of the student
     * @return List of favorite courses
     */
    public static List<Course> getFavoriteCourses(int studentId) {
        Connection conn = DataBaseConnection.getConnection();
        List<Course> favoriteCourses = new ArrayList<>();
        
        String sql = "SELECT c.* FROM course c " +
                    "INNER JOIN favorite_courses fc ON c.id = fc.course_id " +
                    "WHERE fc.student_id = ? " +
                    "ORDER BY fc.created_at DESC";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
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
                course.setPdfPath(rs.getString("pdf_path"));
                course.setTargetLevel(rs.getString("target_level"));
                favoriteCourses.add(course);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return favoriteCourses;
    }
} 
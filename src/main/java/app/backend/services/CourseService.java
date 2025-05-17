package app.backend.services;

import app.backend.database.DataBaseConnection;
import app.backend.models.Course;
import app.backend.models.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseService {

    // Add a new course
    public static boolean addCourse(Course course) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "INSERT INTO Course (title, description, comment, teacher_id, pdf_path, target_level) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, course.getTitle());
            stmt.setString(2, course.getDescription());
            stmt.setString(3, course.getComment());
            stmt.setInt(4, course.getTeacherId());
            stmt.setString(5, course.getPdfPath());
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
        boolean success = false;
        
        try {
            // Start transaction
            conn.setAutoCommit(false);
            
            // 1. Update the course
            String sql = "UPDATE Course SET title = ?, description = ?, comment = ?, pdf_path = ?, target_level = ? WHERE id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, course.getTitle());
                stmt.setString(2, course.getDescription());
                stmt.setString(3, course.getComment());
                stmt.setString(4, course.getPdfPath());
                stmt.setString(5, course.getTargetLevel());
                stmt.setInt(6, course.getId());
                
                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated <= 0) {
                    throw new SQLException("Failed to update course");
                }
            }
            
            // 2. Update exercises' target level
            String updateExercisesSql = "UPDATE exercice SET target_level = ? WHERE course_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateExercisesSql)) {
                stmt.setString(1, course.getTargetLevel());
                stmt.setInt(2, course.getId());
                stmt.executeUpdate();
            }
            
            // 3. Update practical works' target level
            String updatePracticalWorksSql = "UPDATE PracticalWork SET target_level = ? WHERE course_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updatePracticalWorksSql)) {
                stmt.setString(1, course.getTargetLevel());
                stmt.setInt(2, course.getId());
                stmt.executeUpdate();
            }

            // 4. Quizzes don't have a target_level column, they inherit from course
            
            // Commit the transaction
            conn.commit();
            success = true;
            
        } catch (SQLException e) {
            // Roll back the transaction if something fails
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            success = false;
        } finally {
            // Reset auto-commit to default state
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        
        return success;
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
                course.setPdfPath(rs.getString("pdf_path"));
                course.setTargetLevel(rs.getString("target_level"));
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
                return course;
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
                course.setPdfPath(rs.getString("pdf_path"));
                course.setTargetLevel(rs.getString("target_level"));
                courses.add(course);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return courses;
    }

    // Get courses by target level or all if the user is a teacher
    public static List<Course> getCoursesByEnrollmentLevel(String level, boolean isTeacher) {
        // If user is a teacher, return all courses
        if (isTeacher) {
            return getAllCourses();
        }
        
        Connection conn = DataBaseConnection.getConnection();
        List<Course> courses = new ArrayList<>();

        // SQL to get courses for specific level or with null level (available to all)
        String sql = "SELECT * FROM Course WHERE target_level = ? OR target_level IS NULL ORDER BY created_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, level);
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
                courses.add(course);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return courses;
    }
    
    /**
     * Get courses filtered by both teacher ID and student enrollment level
     * 
     * @param teacherId The ID of the teacher whose courses to retrieve
     * @param level The enrollment level of the student
     * @return List of matching courses
     */
    public static List<Course> getCoursesByTeacherAndLevel(int teacherId, String level) {
        Connection conn = DataBaseConnection.getConnection();
        List<Course> courses = new ArrayList<>();

        // SQL to get courses for specific teacher that match the level or have no level specified
        String sql = "SELECT * FROM Course WHERE teacher_id = ? AND (target_level = ? OR target_level IS NULL) ORDER BY created_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            stmt.setString(2, level);
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
                courses.add(course);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return courses;
    }
    
    /**
     * Get count of courses by teacher ID
     * 
     * @param teacherId The ID of the teacher
     * @return Number of courses
     */
    public static int getCourseCountByTeacher(int teacherId) {
        Connection conn = DataBaseConnection.getConnection();
        int count = 0;

        String sql = "SELECT COUNT(*) as count FROM Course WHERE teacher_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
    }

    /**
     * Get count of courses available for a specific student level
     * 
     * @param level The enrollment level of the student
     * @return Number of courses
     */
    public static int getCourseCountByLevel(String level) {
        Connection conn = DataBaseConnection.getConnection();
        int count = 0;

        String sql = "SELECT COUNT(*) as count FROM Course WHERE target_level = ? OR target_level IS NULL";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, level);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
    }
    
    /**
     * Get teachers who have courses for a specific educational level
     * 
     * @param studentLevel The student's educational level
     * @return List of teachers with courses matching the level
     */
    public static List<User> getTeachersWithCoursesByLevel(String studentLevel) {
        Connection conn = DataBaseConnection.getConnection();
        List<User> teachers = new ArrayList<>();

        String sql = "SELECT DISTINCT u.* FROM User u " +
                    "JOIN Course c ON u.id = c.teacher_id " +
                    "WHERE u.role = 'teacher' " +
                    "AND (c.target_level = ? OR c.target_level IS NULL) " +
                    "ORDER BY u.name";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentLevel);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                User teacher = new User(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("password"),
                    rs.getString("matricule"),
                    rs.getString("role"),
                    rs.getTimestamp("created_at"),
                    rs.getString("enrollment_level"),
                    rs.getString("university_name")
                );
                teachers.add(teacher);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return teachers;
    }
}

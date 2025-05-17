package app.backend.services;

import app.backend.database.DataBaseConnection;
import app.backend.models.PracticalWork;
import app.backend.models.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PracticalWorkService {

    // Add a new practical work
    public static boolean addPracticalWork(PracticalWork practicalWork) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "INSERT INTO PracticalWork (course_id, title, description, comment, deadline, teacher_id, pdf_path, target_level) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, practicalWork.getCourseId());
            stmt.setString(2, practicalWork.getTitle());
            stmt.setString(3, practicalWork.getDescription());
            stmt.setString(4, practicalWork.getComment());
            stmt.setDate(5, practicalWork.getDeadline());
            stmt.setInt(6, practicalWork.getTeacherId());
            stmt.setString(7, practicalWork.getPdfPath());
            stmt.setString(8, practicalWork.getTargetLevel());

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

        String sql = "UPDATE PracticalWork SET title = ?, description = ?, comment = ?, course_id = ?, deadline = ?, pdf_path = ?, target_level = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, practicalWork.getTitle());
            stmt.setString(2, practicalWork.getDescription());
            stmt.setString(3, practicalWork.getComment());
            stmt.setInt(4, practicalWork.getCourseId());
            stmt.setDate(5, practicalWork.getDeadline());
            stmt.setString(6, practicalWork.getPdfPath());
            stmt.setString(7, practicalWork.getTargetLevel());
            stmt.setInt(8, practicalWork.getId());

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
                    rs.getTimestamp("created_at"),
                    rs.getInt("teacher_id"),
                    rs.getString("pdf_path"),
                    rs.getString("target_level")
                );
                works.add(work);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return works;
    }

    // Get all practical works for a teacher
    public static List<PracticalWork> getPracticalWorksByTeacherId(int teacherId) {
        Connection conn = DataBaseConnection.getConnection();
        List<PracticalWork> works = new ArrayList<>();

        String sql = "SELECT * FROM PracticalWork WHERE teacher_id = ? ORDER BY created_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                PracticalWork work = new PracticalWork(
                    rs.getInt("id"),
                    rs.getInt("course_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("comment"),
                    rs.getDate("deadline"),
                    rs.getTimestamp("created_at"),
                    rs.getInt("teacher_id"),
                    rs.getString("pdf_path"),
                    rs.getString("target_level")
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
                    rs.getTimestamp("created_at"),
                    rs.getInt("teacher_id"),
                    rs.getString("pdf_path"),
                    rs.getString("target_level")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Get count of practical works by teacher ID
    public static int getPracticalWorkCountByTeacher(int teacherId) {
        Connection conn = DataBaseConnection.getConnection();
        int count = 0;

        String sql = "SELECT COUNT(*) FROM PracticalWork WHERE teacher_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
    }

    /**
     * Get count of practical works available for a specific student level
     * 
     * @param level The enrollment level of the student
     * @return Number of practical works
     */
    public static int getPracticalWorkCountByLevel(String level) {
        Connection conn = DataBaseConnection.getConnection();
        int count = 0;

        String sql = "SELECT COUNT(*) FROM PracticalWork WHERE target_level = ? OR target_level IS NULL";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, level);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
    }
    
    /**
     * Get teachers who have practical works for a specific educational level
     * 
     * @param studentLevel The student's educational level
     * @return List of teachers with practical works matching the level
     */
    public static List<User> getTeachersWithPracticalWorksByLevel(String studentLevel) {
        Connection conn = DataBaseConnection.getConnection();
        List<User> teachers = new ArrayList<>();

        String sql = "SELECT DISTINCT u.* FROM User u " +
                    "JOIN PracticalWork pw ON pw.teacher_id = u.id " +
                    "WHERE u.role = 'teacher' " +
                    "AND pw.target_level = ? " +
                    "ORDER BY u.name";

        System.out.println("Executing SQL to find teachers with practical works for level: " + studentLevel);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentLevel);
            ResultSet rs = stmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                count++;
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
                System.out.println("Found teacher with practical works: " + teacher.getName() + " (ID: " + teacher.getId() + ")");
            }
            System.out.println("Found " + count + " teachers with practical works for level " + studentLevel);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return teachers;
    }
    
    /**
     * Get practical works filtered by both teacher ID and student enrollment level
     * 
     * @param teacherId The ID of the teacher whose practical works to retrieve
     * @param level The enrollment level of the student
     * @return List of matching practical works
     */
    public static List<PracticalWork> getPracticalWorksByTeacherAndLevel(int teacherId, String level) {
        Connection conn = DataBaseConnection.getConnection();
        List<PracticalWork> works = new ArrayList<>();

        // SQL to get practical works for specific teacher that match the level
        String sql = "SELECT * FROM PracticalWork " +
                    "WHERE teacher_id = ? " +
                    "AND target_level = ? " +
                    "ORDER BY created_at DESC";

        System.out.println("Executing SQL to find practical works for teacher ID: " + teacherId + " and level: " + level);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            stmt.setString(2, level);
            ResultSet rs = stmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                count++;
                PracticalWork work = new PracticalWork(
                    rs.getInt("id"),
                    rs.getInt("course_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("comment"),
                    rs.getDate("deadline"),
                    rs.getTimestamp("created_at"),
                    rs.getInt("teacher_id"),
                    rs.getString("pdf_path"),
                    rs.getString("target_level")
                );
                works.add(work);
                System.out.println("Found practical work: '" + work.getTitle() + "' with level: " + work.getTargetLevel());
            }
            System.out.println("Found " + count + " practical works for teacher ID: " + teacherId + " and level: " + level);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return works;
    }
}

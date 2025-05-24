package app.backend.services;

import app.backend.database.DataBaseConnection;
import app.backend.models.Course;
import app.backend.models.Quiz;
import app.backend.models.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizService {

    // Add a new quiz
    public static boolean addQuiz(Quiz quiz) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "INSERT INTO Quiz (course_id, title, description, comment) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quiz.getCourseId());
            stmt.setString(2, quiz.getTitle());
            stmt.setString(3, quiz.getDescription());
            stmt.setString(4, quiz.getComment());

            int rowsInserted = stmt.executeUpdate();
            
            // If quiz was added successfully, get the course and send notifications
            if (rowsInserted > 0) {
                // Get the course target level and teacher ID
                Course course = CourseService.getCourseById(quiz.getCourseId());
            }
            
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update an existing quiz
    public static boolean updateQuiz(Quiz quiz) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "UPDATE Quiz SET title = ?, description = ?, comment = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, quiz.getTitle());
            stmt.setString(2, quiz.getDescription());
            stmt.setString(3, quiz.getComment());
            stmt.setInt(4, quiz.getId());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete a quiz
    public static boolean deleteQuiz(int quizId) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "DELETE FROM Quiz WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quizId);

            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all quizzes for a course
    public static List<Quiz> getQuizzesByCourseId(int courseId) {
        Connection conn = DataBaseConnection.getConnection();
        List<Quiz> quizzes = new ArrayList<>();

        String sql = "SELECT * FROM Quiz WHERE course_id = ? ORDER BY created_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Quiz quiz = new Quiz(
                    rs.getInt("id"),
                    rs.getInt("course_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("comment"),
                    rs.getTimestamp("created_at")
                );
                quizzes.add(quiz);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return quizzes;
    }

    // Get a quiz by ID
    public static Quiz getQuizById(int quizId) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "SELECT * FROM Quiz WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quizId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Quiz(
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

    // Get all quizzes
    public static List<Quiz> getAllQuizzes() {
        Connection conn = DataBaseConnection.getConnection();
        List<Quiz> quizzes = new ArrayList<>();

        String sql = "SELECT * FROM Quiz ORDER BY created_at DESC";

        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Quiz quiz = new Quiz(
                    rs.getInt("id"),
                    rs.getInt("course_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("comment"),
                    rs.getTimestamp("created_at")
                );
                quizzes.add(quiz);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return quizzes;
    }
    
    // Get quizzes by enrollment level
    public static List<Quiz> getQuizzesByEnrollmentLevel(String level) {
        Connection conn = DataBaseConnection.getConnection();
        List<Quiz> quizzes = new ArrayList<>();

        // Get quizzes from courses that match the given enrollment level
        String sql = "SELECT q.* FROM Quiz q " +
                     "INNER JOIN Course c ON q.course_id = c.id " +
                     "WHERE c.target_level = ? " +
                     "ORDER BY q.created_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, level);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Quiz quiz = new Quiz(
                    rs.getInt("id"),
                    rs.getInt("course_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("comment"),
                    rs.getTimestamp("created_at")
                );
                quizzes.add(quiz);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return quizzes;
    }
    
    /**
     * Get teachers who have quizzes for a specific educational level
     * 
     * @param studentLevel The student's educational level
     * @return List of teachers with quizzes matching the level
     */
    public static List<User> getTeachersWithQuizzesByLevel(String studentLevel) {
        Connection conn = DataBaseConnection.getConnection();
        List<User> teachers = new ArrayList<>();

        String sql = "SELECT DISTINCT u.* FROM User u " +
                    "JOIN Course c ON u.id = c.teacher_id " +
                    "JOIN Quiz q ON c.id = q.course_id " +
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

    // Get all quizzes by teacher ID
    public static List<Quiz> getQuizzesByTeacherId(int teacherId) {
        Connection conn = DataBaseConnection.getConnection();
        List<Quiz> quizzes = new ArrayList<>();

        String sql = "SELECT q.* FROM Quiz q " +
                     "INNER JOIN Course c ON q.course_id = c.id " +
                     "WHERE c.teacher_id = ? " +
                     "ORDER BY q.created_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Quiz quiz = new Quiz(
                    rs.getInt("id"),
                    rs.getInt("course_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("comment"),
                    rs.getTimestamp("created_at")
                );
                quizzes.add(quiz);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return quizzes;
    }
    
    // Get count of quizzes by teacher ID
    public static int getQuizCountByTeacher(int teacherId) {
        Connection conn = DataBaseConnection.getConnection();
        int count = 0;

        String sql = "SELECT COUNT(*) FROM Quiz q " +
                     "INNER JOIN Course c ON q.course_id = c.id " +
                     "WHERE c.teacher_id = ?";

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
     * Get count of quizzes available for a specific student level
     * 
     * @param level The enrollment level of the student
     * @return Number of quizzes
     */
    public static int getQuizCountByLevel(String level) {
        Connection conn = DataBaseConnection.getConnection();
        int count = 0;

        String sql = "SELECT COUNT(*) FROM Quiz q " +
                     "INNER JOIN Course c ON q.course_id = c.id " +
                     "WHERE c.target_level = ? OR c.target_level IS NULL";

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
}

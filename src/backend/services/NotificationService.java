package backend.services;

import backend.database.DataBaseConnection;
import backend.models.Notification;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationService {

    // Add a new notification
    public static boolean sendNotification(Notification notification) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "INSERT INTO Notification (user_id, message) VALUES (?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notification.getUserId());
            stmt.setString(2, notification.getMessage());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all notifications for a user
    public static List<Notification> getNotificationsByUserId(int userId) {
        Connection conn = DataBaseConnection.getConnection();
        List<Notification> notifications = new ArrayList<>();

        String sql = "SELECT * FROM Notification WHERE user_id = ? ORDER BY created_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Notification notification = new Notification(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("message"),
                    rs.getBoolean("seen"),
                    rs.getTimestamp("created_at")
                );
                notifications.add(notification);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return notifications;
    }

    // Mark a notification as seen
    public static boolean markAsSeen(int notificationId) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "UPDATE Notification SET seen = TRUE WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notificationId);

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Mark all notifications as seen for a user
    public static boolean markAllAsSeenByUserId(int userId) {
        Connection conn = DataBaseConnection.getConnection();

        String sql = "UPDATE Notification SET seen = TRUE WHERE user_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

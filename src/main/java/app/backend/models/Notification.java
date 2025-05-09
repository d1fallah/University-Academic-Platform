package app.backend.models;

import java.sql.Timestamp;

public class Notification {

    private int id;
    private int userId;
    private String message;
    private boolean seen;
    private Timestamp createdAt;

    // Constructors
    public Notification() {}

    public Notification(int id, int userId, String message, boolean seen, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.seen = seen;
        this.createdAt = createdAt;
    }

    public Notification(int userId, String message) {
        this.userId = userId;
        this.message = message;
        this.seen = false;  // default new notifications are unseen
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}

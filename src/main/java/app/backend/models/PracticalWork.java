package app.backend.models;

import java.sql.Date;
import java.sql.Timestamp;

public class PracticalWork {

    private int id;
    private int courseId;
    private String title;
    private String description;
    private String comment;
    private Date deadline;
    private Timestamp createdAt;

    // Constructors
    public PracticalWork() {}

    public PracticalWork(int id, int courseId, String title, String description, String comment, Date deadline, Timestamp createdAt) {
        this.id = id;
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.comment = comment;
        this.deadline = deadline;
        this.createdAt = createdAt;
    }

    public PracticalWork(int courseId, String title, String description, String comment, Date deadline) {
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.comment = comment;
        this.deadline = deadline;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}

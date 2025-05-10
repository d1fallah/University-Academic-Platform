package app.backend.models;

import java.sql.Timestamp;

public class Exercise {

    private int id;
    private int courseId;
    private String title;
    private String description;
    private String comment;
    private Timestamp createdAt;
    private String pdfPath;
    private String targetLevel;
    private int teacherId;

    // Constructors
    public Exercise() {}

    public Exercise(int id, int courseId, String title, String description, String comment, Timestamp createdAt) {
        this.id = id;
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public Exercise(int courseId, String title, String description, String comment) {
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.comment = comment;
    }

    // Full constructor with all fields
    public Exercise(int id, int courseId, String title, String description, String comment, 
                   Timestamp createdAt, String pdfPath, String targetLevel, int teacherId) {
        this.id = id;
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.comment = comment;
        this.createdAt = createdAt;
        this.pdfPath = pdfPath;
        this.targetLevel = targetLevel;
        this.teacherId = teacherId;
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

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getPdfPath() {
        return pdfPath;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }
    
    public String getTargetLevel() {
        return targetLevel;
    }
    
    public void setTargetLevel(String targetLevel) {
        this.targetLevel = targetLevel;
    }
    
    public int getTeacherId() {
        return teacherId;
    }
    
    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }
}

package backend.models;

import java.sql.Timestamp;

public class Course {

    private int id;
    private String title;
    private String description;
    private String comment;
    private String pdfPath;    
    private int teacherId;
    private Timestamp createdAt;

    // Constructors
    public Course() {}

    // Full constructor including pdfPath
    public Course(int id, String title, String description, String comment, String pdfPath, int teacherId, Timestamp createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.comment = comment;
        this.pdfPath = pdfPath;
        this.teacherId = teacherId;
        this.createdAt = createdAt;
    }

    // Constructor for adding a course without createdAt and id
    public Course(String title, String description, String comment, String pdfPath, int teacherId) {
        this.title = title;
        this.description = description;
        this.comment = comment;
        this.pdfPath = pdfPath;
        this.teacherId = teacherId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getPdfPath() {
        return pdfPath;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
